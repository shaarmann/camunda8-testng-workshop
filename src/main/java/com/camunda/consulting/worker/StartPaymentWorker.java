package com.camunda.consulting.worker;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class StartPaymentWorker {

  ZeebeClient zeebeClient;

  public StartPaymentWorker(ZeebeClient zeebeClient) {
    this.zeebeClient = zeebeClient;
  }

  @JobWorker
  public void startPayment(ActivatedJob job, JobClient client) {
    zeebeClient.newPublishMessageCommand()
        .messageName("StartPaymentMsg")
        .correlationKey("")
        .variables(job.getVariables())
        .send()
        .join();
  }
}
