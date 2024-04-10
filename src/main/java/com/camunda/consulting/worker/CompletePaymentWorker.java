package com.camunda.consulting.worker;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import io.camunda.zeebe.spring.client.annotation.Variable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CompletePaymentWorker {

  ZeebeClient client;

  public CompletePaymentWorker(ZeebeClient client) {
    this.client = client;
  }

  @JobWorker
  public void completePayment(@Variable String orderId) {
    client.newPublishMessageCommand()
        .messageName("PaymentCompletedMsg")
        .correlationKey(orderId)
        .send()
        .join();
  }
}
