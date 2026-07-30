package com.example;

import org.springframework.stereotype.Component;

@Component //It means objects for this class you should manage
public class PaymentService {

    public void pay(){
        System.out.println("payment done");
    }
}
