package com.camunda.consulting.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class CustomerService {

  public static final Logger LOG = LoggerFactory.getLogger(CustomerService.class);
  public double getCustomerCredit(String customerId) {
    LOG.debug("Determining credit of customer {}", customerId);
    return Double.parseDouble(customerId.substring(customerId.length() - 2));
  }
}
