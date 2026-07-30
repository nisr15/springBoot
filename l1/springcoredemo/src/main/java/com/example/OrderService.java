package com.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.payment.PaymentService;

@Component //It means objects for this class you should manage
public class OrderService {

    //Field Dependency Injection  -- Not recommended
    // @Autowired
    private PaymentService paymentService;

    //Constructor Dependency Injection  -- Most Recommended
    @Autowired
    public OrderService(PaymentService paymentService){
        this.paymentService=paymentService;
    }

    //Setter Dependency Injection
    // @Autowired
    // public void setPaymentService(PaymentService paymentService){
    //     this.paymentService=paymentService;
    // }


    public void placeOrder(){
        paymentService.pay();
        System.out.println("Order placed");
    }
}
