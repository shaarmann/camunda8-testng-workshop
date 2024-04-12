package com.camunda.consulting;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.ZeebeFuture;
import io.camunda.zeebe.client.api.response.ProcessInstanceResult;
import io.camunda.zeebe.client.api.worker.JobWorker;
import io.camunda.zeebe.client.api.worker.JobWorkerMetrics;
import io.zeebe.containers.ZeebeContainer;
import org.testcontainers.shaded.org.awaitility.Awaitility;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class NGOrderProcessTest {
  static final ZeebeContainer zeebeContainer = new ZeebeContainer();

  private final ZeebeClient client;

  public NGOrderProcessTest() {
    zeebeContainer.start();
    client = ZeebeClient.newClientBuilder()
        .gatewayAddress(zeebeContainer.getExternalGatewayAddress())
        .usePlaintext()
        .build();
  }

  @BeforeClass
  private void deployModel() {
    client.newDeployResourceCommand()
        .addResourceFromClasspath("order_process.bpmn")
        .send()
        .join();
  }

  @AfterClass
  private void stopContainer() {
    zeebeContainer.stop();
  }

  @Test
  public void testOrderProcess() {

    client.newDeployResourceCommand()
        .addResourceFromClasspath("order_process.bpmn")
        .send()
        .join();
    // Start the process instance
    ZeebeFuture<ProcessInstanceResult> startCall = client.newCreateInstanceCommand()
        .bpmnProcessId("OrderProcess")
        .latestVersion()
        .variables(
            Map.of("customerId", "cust90",
                "orderTotal", 100D,
                "cardNumber", "1234567890",
                "expiryDate", "12/26",
                "cvc", "123",
                "orderId", "order1"))
        // Future will only be completed once the process completed
        .withResult()
        .send();

    final AtomicInteger numberOfCompletions = new AtomicInteger(0);
    // Create a job worker for the completePayment type
    JobWorker jobWorker = client.newWorker()
        .jobType("startPayment")
        .handler((client, job) -> {
          client.newCompleteCommand(job).send().join();
        }).metrics(new JobWorkerMetrics() {
          @Override
          public void jobHandled(int count) {
            numberOfCompletions.addAndGet(count);
          }
        }).open();

    // Check if the worker has been executed
    Awaitility.await().untilAsserted(() -> Assert.assertEquals(numberOfCompletions.get(), 1));

    // Send the completion message
    client.newPublishMessageCommand()
        .messageName("PaymentCompletedMsg")
        .correlationKey("order1")
        .send()
        .join();

    // Wait for the process to complete
    startCall.join();
    // Close the worker
    jobWorker.close();
  }
}
