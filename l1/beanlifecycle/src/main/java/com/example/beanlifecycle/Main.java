package com.example.beanlifecycle;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class Main {
    public static void main(String[] args) {
        ApplicationContext context=new AnnotationConfigApplicationContext(AppConfig.class);

        // OrderService order=context.getBean(OrderService.class);
        // order.placeOrder();
        CartService cs=context.getBean(CartService.class);
        System.out.println(cs.getName(1));
    }
}