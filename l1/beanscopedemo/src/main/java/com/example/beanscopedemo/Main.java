package com.example.beanscopedemo;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class Main {
    public static void main(String[] args) {
        ApplicationContext context=new AnnotationConfigApplicationContext(AppConfig.class);

        OrderService order=context.getBean(OrderService.class);
        order.placeOrder();
        OrderService order1=context.getBean(OrderService.class);
        if(order==order1){
            System.out.println("true");
        }
        //IOC by default created only one bean for one bean defination , which is singleton
        
    }
}