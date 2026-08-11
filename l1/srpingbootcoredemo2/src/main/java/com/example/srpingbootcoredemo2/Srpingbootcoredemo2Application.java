package com.example.srpingbootcoredemo2;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
@SpringBootApplication
public class Srpingbootcoredemo2Application {

	public static void main(String[] args) {
		ApplicationContext context=SpringApplication.run(Srpingbootcoredemo2Application.class, args);
		
		// OrderService order=context.getBean(OrderService.class);
		// order.placeOrder();


		// PaymentGateway paymentGateway=context.getBean(PaymentGateway.class);
		// paymentGateway.setType("Paytm");
		// paymentGateway.setRetryCount(5);

		// System.out.println(paymentGateway.getType());
		// System.out.println(paymentGateway.getRetryCount());
		// System.out.println(paymentGateway.isEnabled());
		// System.out.println(paymentGateway.getTimeout());
	}

	// @Bean
	// public JsonParser getJsonParserBean(){
	// 	return new BasicJsonParser();
	// }

}
