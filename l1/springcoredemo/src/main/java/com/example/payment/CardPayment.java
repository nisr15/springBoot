package com.example.payment;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;


// @Component
// @Qualifier // Tells this class is Qualified to have a bean, and winner is selected at the dependency injection (in other class)
// @Qualifier("CP")  // by default Bean is camel case of class name , if you need other you can specify here
public class CardPayment implements PaymentService{

    @Override
    public void pay(){
        System.out.println("payment done via card");
    }
}
