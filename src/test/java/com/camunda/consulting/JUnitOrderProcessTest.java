package com.camunda.consulting;

import com.camunda.consulting.worker.StartPaymentWorker;
import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.process.test.assertions.BpmnAssert;
import io.camunda.zeebe.process.test.inspections.InspectionUtility;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import io.camunda.zeebe.spring.test.ZeebeSpringTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import java.util.Map;

import static io.camunda.zeebe.spring.test.ZeebeTestThreadSupport.waitForProcessInstanceCompleted;
import static io.camunda.zeebe.spring.test.ZeebeTestThreadSupport.waitForProcessInstanceHasPassedElement;

@SpringBootTest
@ZeebeSpringTest
public class JUnitOrderProcessTest {

  @Autowired
  ZeebeClient client;

  // Use a test configuration to override the StartPaymentWorker
  @TestConfiguration
  public static class OverrideStartPaymentWorker {
    @Bean
    public StartPaymentWorker startPaymentWorker() {
      return new StartPaymentWorker(null) {
        @Override
        @JobWorker
        public void startPayment(ActivatedJob job, JobClient client) {
          // Do Nothing
        }
      };
    }
  }

  @Test
  public void testOrderProcess() {
    // Start a new instance of the order process
    ProcessInstanceEvent processInstance = client.newCreateInstanceCommand()
        .bpmnProcessId("OrderProcess")
        .latestVersion()
        .variables(
            Map.of("customerId", "cust90", "orderTotal", 90D, "cardNumber", "1234567890", "expiryDate", "12/26",
                "cvc", "123", "orderId", "order1"))
        .send()
        .join();

    // Wait for the process to reach the message receive event
    waitForProcessInstanceHasPassedElement(processInstance,"Activity_0c7xp04");
    // Send the message
    client.newPublishMessageCommand()
        .messageName("PaymentCompletedMsg")
        .correlationKey("order1")
        .send()
        .join();
    // Wait for the completion of the process instance
    waitForProcessInstanceCompleted(processInstance);

    // Make assertions about the process execution
    BpmnAssert.assertThat(processInstance)
        .hasPassedElementsInOrder(
            "Activity_0c7xp04",
            "Event_1x2l6vp")
        .isCompleted();

    // Make sure that the Payment Process has not been called
    Assertions.assertTrue(InspectionUtility.findProcessInstances()
        .withBpmnProcessId("PaymentProcessMsg")
        .findLastProcessInstance()
        .isEmpty());
  }
}
