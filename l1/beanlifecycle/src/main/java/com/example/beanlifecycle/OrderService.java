package com.example.beanlifecycle;

import org.springframework.stereotype.Component;

@Component
public class OrderService {

    private PaymentService paymentService;

    public OrderService(PaymentService paymentService){
        this.paymentService=paymentService;
    }

    public void placeOrder(){
        System.out.println("Order placec");
        paymentService.pay();
    }

}
