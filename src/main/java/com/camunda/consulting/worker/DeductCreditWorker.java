package com.camunda.consulting.worker;

import com.camunda.consulting.services.CustomerService;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import io.camunda.zeebe.spring.client.annotation.Variable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class DeductCreditWorker {

  private static final Logger LOG = LoggerFactory.getLogger(DeductCreditWorker.class);

  private CustomerService service;

  public DeductCreditWorker(CustomerService service) {
    this.service = service;
  }

  @JobWorker
  public Map<String, Double> deductCredit(@Variable String customerId, @Variable Double orderTotal) {
    LOG.info("Invoking deduct credit worker");
    double customerCredit = service.getCustomerCredit(customerId);
    double openAmount = Math.max(0D, orderTotal - customerCredit);
    return Map.of("openAmount", openAmount);
  }
}