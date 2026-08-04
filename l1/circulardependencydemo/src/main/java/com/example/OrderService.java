package com.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class OrderService {

    @Autowired   //Here we are using setter,getter to overcome the circular dependency with constructor
    private PaymentService paymentService;

    // public OrderService(PaymentService paymentService){
    //     this.paymentService=paymentService;
    // }

    public void placeOrder(){
        paymentService.pay();
        System.out.println("Order placed");
    }

    public void getOrderDetails(){
        System.out.println("Order Details");
    }
}
