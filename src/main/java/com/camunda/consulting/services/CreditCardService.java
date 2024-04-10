package com.camunda.consulting.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class CreditCardService {
  public static final Logger LOG = LoggerFactory.getLogger(CreditCardService.class);

  public void chargeCreditCard(String cardNumber, String cvc, String expiryDate, Double openAmount) {
    LOG.info("Charging {} of credit card {}", openAmount, cardNumber);
  }
}
