package com.camunda.consulting;

import io.camunda.zeebe.spring.client.annotation.Deployment;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Deployment(resources = {"classpath:payment_process.bpmn", "classpath:payment_process_msgstart.bpmn", "classpath:order_process.bpmn"})
public class App {
  public static void main(String[] args) {
    SpringApplication.run(App.class, args);
  }
}
