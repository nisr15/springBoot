package com.example;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class Main {
    public static void main(String[] args) {
        //ApplicationContext -> means IOC container
        ApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class); //Create IOC container using annotation based config, config rules/info will get from AppConfig
        OrderService order=context.getBean(OrderService.class);
        order.placeOrder();

        // PaymentService pay=context.getBean(PaymentService.class);
        // pay.pay();
    }
}