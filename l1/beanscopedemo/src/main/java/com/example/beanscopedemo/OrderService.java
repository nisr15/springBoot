package com.example.beanscopedemo;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("singleton")  // IOC container gets up,follows process of getting bean defination and directly creates the bean this is called eager intiallization can be changed to Lazy using @Lazy
// @Scope("prototype")  // IOC container gets up , get the bean definations , but only creates beans when they are required. follows lazy instialization cannot be changed to eager.
public class OrderService {
    

    public OrderService(){
        System.out.println("Order service created");
    }

    public void placeOrder(){
        
        System.out.println("Order placed");
    }
}
