package com.example.learn;

import com.example.learn.notification.NotificationService;
import com.example.learn.notification.SmsService;

public class OrderService {

    NotificationService notification;

    public OrderService(NotificationService notification){
        this.notification=notification;
    }

    public void placeOrder(){
        System.out.println("Order placed");
        this.notification.sendNotification();
    }
}
