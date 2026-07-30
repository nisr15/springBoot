package com.example.payment;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component
@Primary
public class Upi implements PaymentService{

    @Override
    public void pay(){
        System.out.println("payment done via upi");
    }
}
