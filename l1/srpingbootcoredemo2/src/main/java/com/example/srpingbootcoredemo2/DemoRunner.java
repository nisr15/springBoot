package com.example.srpingbootcoredemo2;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DemoRunner implements CommandLineRunner /*ApplicationRunner*/{


    PaymentGateway paymentGateway;

    public DemoRunner(PaymentGateway paymentGateway){
        this.paymentGateway=paymentGateway;
    }

    @Override
    public void run(String... args) throws Exception {
        paymentGateway.print();
    }

    // @Override
    // public void run(ApplicationArguments args) throws Exception {
    //     paymentGateway.print();
        
    // }

    

    

}
