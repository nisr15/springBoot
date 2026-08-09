package com.example.beanscopedemo;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan
public class AppConfig {
    
    @Bean
    public OrderService getOrder(){
        return new OrderService();
    }

    @Bean
    public OrderService getOrder2(){
        return new OrderService();
    }

    //Here we are giving 2 bean definations for the IOC container , so created 2 different objects
    // Which work when class is singleton

}
