package com.camunda.consulting.worker;

import com.camunda.consulting.services.CreditCardService;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import io.camunda.zeebe.spring.client.annotation.Variable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ChargeCreditCardWorker {

  private static final Logger LOG = LoggerFactory.getLogger(ChargeCreditCardWorker.class);
  private final CreditCardService service;

  public ChargeCreditCardWorker(CreditCardService service) {
    this.service = service;
  }

  @JobWorker
  public void chargeCreditCard(@Variable Double openAmount, @Variable String cardNumber, @Variable String cvc, @Variable String expiryDate) {
    LOG.info("Invoking charge credit card worker.");
    service.chargeCreditCard(cardNumber, cvc, expiryDate, openAmount);
  }
}