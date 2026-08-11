package com.example.srpingbootcoredemo2;

import org.springframework.stereotype.Component;

@Component
public class OrderService {
    
    private PaymentService paymentService;

    public OrderService(PaymentService paymentService){
        this.paymentService=paymentService;
        System.out.println("Order service created");
    }

    public void placeOrder(){
        
        System.out.println("Order placed");
    }
}
