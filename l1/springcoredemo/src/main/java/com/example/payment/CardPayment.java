package com.example.payment;

import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Qualifier;


@Component
@Qualifier // Tells this class is Qualified to have a bean, and winner is selected at the dependency injection (in other class)
public class CardPayment implements PaymentService{

    @Override
    public void pay(){
        System.out.println("payment done via card");
    }
}
