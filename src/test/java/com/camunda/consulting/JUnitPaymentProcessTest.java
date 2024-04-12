package com.camunda.consulting;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.PublishMessageResponse;
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
public class JUnitPaymentProcessTest {

  @Autowired
  ZeebeClient client;

  @Test
  public void testPaymentProcessSufficientCredit() {
    // Start a new instance of the payment process by sending a corresponding message
    PublishMessageResponse message = client.newPublishMessageCommand()
        .messageName("StartPaymentMsg")
        .correlationKey("")
        .variables(
            Map.of("customerId", "cust90",
                "orderTotal", 90D,
                "cardNumber", "1234567890",
                "expiryDate", "12/26",
                "cvc", "123",
                "orderId", "order1"))
        .send()
        .join();

    // Use the InspectionUtility to retrieve the process instance
    InspectedProcessInstance pi = InspectionUtility.findProcessInstances().findLastProcessInstance().get();
    // Wait for the completion of the process instance
    waitForProcessInstanceCompleted(pi.getProcessInstanceKey());

    // Make assertions about the process execution
    BpmnAssert.assertThat(pi)
        .hasPassedElementsInOrder(
            "Activity_1gpm16w",
            "Event_14vyh2w"
        ).hasNotPassedElement("Activity_1q07ku7")
        .isCompleted();
  }

  @Test
  public void testPaymentProcessInsufficientCredit() {
    // Start a new instance of the payment process by sending a corresponding message
    PublishMessageResponse message = client.newPublishMessageCommand()
        .messageName("StartPaymentMsg")
        .correlationKey("")
        .variables(
            Map.of("customerId", "cust90",
                "orderTotal", 100D,
                "cardNumber", "1234567890",
                "expiryDate", "12/26",
                "cvc", "123",
                "orderId", "order1"))
        .send()
        .join();

    // Use the InspectionUtility to retrieve the process instance
    InspectedProcessInstance pi = InspectionUtility.findProcessInstances().findLastProcessInstance().get();
    // Wait for the completion of the process instance
    waitForProcessInstanceCompleted(pi.getProcessInstanceKey());

    // Make assertions about the process execution
    BpmnAssert.assertThat(pi)
        .hasPassedElementsInOrder(
            "Activity_1gpm16w",
            "Activity_1q07ku7",
            "Event_14vyh2w")
        .isCompleted();
  }
}
