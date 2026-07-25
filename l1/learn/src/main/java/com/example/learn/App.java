package com.example.learn;

import com.example.learn.OrderService;
import com.example.learn.notification.NotificationService;
import com.example.learn.notification.SmsService;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        NotificationService notification=new SmsService();
        OrderService order=new OrderService(notification);
        order.placeOrder();
    }
}
