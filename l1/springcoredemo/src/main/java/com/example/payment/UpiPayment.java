package com.example.payment;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component  //if @primay or @Qualifier is not used we get error as IOC confuse to choose
// @Primary //Tells this is the primary in the implementation of interface and uses it to autowire
@Qualifier  // Tells this class is Qualified to have a bean, and winner is selected at the dependency injection (in other class)
public class UpiPayment implements PaymentService{

    @Override
    public void pay(){
        System.out.println("payment done via upi");
    }
}
