package com.camunda.consulting;

import com.camunda.consulting.services.CreditCardService;
import com.camunda.consulting.services.CustomerService;
import com.camunda.consulting.worker.CompletePaymentWorker;
import io.camunda.zeebe.client.ZeebeClient;
import io.zeebe.containers.ZeebeContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import java.util.Map;

import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
public class NGPaymentProcessTest extends AbstractTestNGSpringContextTests {

  static ZeebeContainer zeebeContainer = new ZeebeContainer();

  @Autowired
  ZeebeClient client;

  // Override the gateway address with the testcontainers address
  @DynamicPropertySource
  static void dataSourceProperties(DynamicPropertyRegistry registry) {
    zeebeContainer.start();
    registry.add("zeebe.client.broker.gateway-address", zeebeContainer::getExternalGatewayAddress);
  }

  @AfterClass
  private void stopContainer() {
    zeebeContainer.stop();
  }

  @SpyBean
  CustomerService customerService;

  @SpyBean
  CreditCardService creditCardService;

  @SpyBean
  CompletePaymentWorker completePaymentWorker;

  @Test
  public void testSufficientCredit() {
    client.newPublishMessageCommand()
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

    await().untilAsserted(() -> verify(customerService, times(1)).getCustomerCredit("cust90"));
    await().untilAsserted(() -> verify(completePaymentWorker, times(1)).completePayment("order1"));
    verify(creditCardService, times(0)).chargeCreditCard("1234567890", "123", "12/26", 0D);
  }

  @Test
  public void testInufficientCredit() {
    client.newPublishMessageCommand()
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

    await().untilAsserted(() -> verify(customerService, times(1)).getCustomerCredit("cust90"));
    await().untilAsserted(() -> verify(creditCardService, times(1)).chargeCreditCard("1234567890", "123", "12/26", 10D));
    await().untilAsserted(() -> verify(completePaymentWorker, times(1)).completePayment("order1"));
  }
}
