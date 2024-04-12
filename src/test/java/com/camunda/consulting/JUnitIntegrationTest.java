package com.camunda.consulting;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;
import io.camunda.zeebe.process.test.assertions.BpmnAssert;
import io.camunda.zeebe.process.test.inspections.InspectionUtility;
import io.camunda.zeebe.process.test.inspections.model.InspectedProcessInstance;
import io.camunda.zeebe.spring.test.ZeebeSpringTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Map;

import static io.camunda.zeebe.spring.test.ZeebeTestThreadSupport.waitForProcessInstanceCompleted;

@SpringBootTest
@ZeebeSpringTest
public class JUnitIntegrationTest {
  @Autowired
  ZeebeClient client;
  
  @Test
  public void testIntegration() {
    // Start a new instance of the order process
    ProcessInstanceEvent orderInstance = client.newCreateInstanceCommand()
        .bpmnProcessId("OrderProcess")
        .latestVersion()
        .variables(
            Map.of("customerId", "cust90", "orderTotal", 90D, "cardNumber", "1234567890", "expiryDate", "12/26", "cvc",
                "123", "orderId", "order1"))
        .send()
        .join();
    // Wait for the completion of the process instance
    waitForProcessInstanceCompleted(orderInstance);

    // Make assertions about the process execution
    BpmnAssert.assertThat(orderInstance)
        .hasPassedElementsInOrder(
            "Activity_0c7xp04",
            "Event_1x2l6vp")
        .isCompleted();

    // Retrieve the instance of the payment process
    InspectedProcessInstance paymentInstance = InspectionUtility.findProcessInstances()
        .withBpmnProcessId("PaymentProcessMsg")
        .findLastProcessInstance()
        .get();

    // Make assertions about the payment process
    BpmnAssert.assertThat(paymentInstance)
        .isCompleted()
        .hasPassedElementsInOrder(
            "Activity_1gpm16w",
            "Event_14vyh2w")
        .hasNotPassedElement("Activity_1q07ku7");
  }


}
