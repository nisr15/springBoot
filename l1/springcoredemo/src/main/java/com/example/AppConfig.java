package com.example;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.beans.factory.annotation.Qualifier;

import com.example.payment.CardPayment;
import com.example.payment.PaymentService;
import com.example.payment.UpiPayment;

@Configuration  // Telling it is a config file
@ComponentScan("com.example")  // scans which classes has @component , which should it handle
public class AppConfig {
    

    @Bean //Tells IOC container to create object for class User with this method and manage the object just like other object , issue was with only create so developer handled it
    public User createUser(){
        return new User("Indra",23);
    }


    @Bean
    // @Primary //one way
    @Qualifier("cp")
    public PaymentService createCardpayment(){
        return new CardPayment();
    }

    @Bean //if both @Bean and @Component is specified Bean takes priority
    @Qualifier("upi")
    public PaymentService createUpipayment(){
        return new UpiPayment();
    }

    @Bean 
    public OrderService creatOrderService(@Qualifier("upi") PaymentService payment){
        return new OrderService(payment);
    }
}
