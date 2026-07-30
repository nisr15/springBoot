package com.example;

import org.springframework.stereotype.Component;

@Component //It means objects for this class you should manage
public class OrderService {



    public void placeOrder(){
        System.out.println("Order placed");
    }
}
