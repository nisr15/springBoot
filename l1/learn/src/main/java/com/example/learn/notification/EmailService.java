package com.example.learn.notification;

public class EmailService implements NotificationService {

    @Override
    public void sendNotification(){
        System.out.println("Email Notification sent");
    }
}
