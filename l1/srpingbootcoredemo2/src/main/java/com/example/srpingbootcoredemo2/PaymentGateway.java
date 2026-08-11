package com.example.srpingbootcoredemo2;

import org.springframework.stereotype.Component;

@Component
public class PaymentGateway {

    // @Value("${paymentGateway.type:Phonepe}")
    // private String type;

    // @Value("${paymentGateway.retryCnt}")
    // private int retryCount;

    // public PaymentGateway(@Value("${paymentGateway.retryCnt}") int retryCount, @Value("${paymentGateway.type}") String type) {
    //     this.retryCount = retryCount;
    //     this.type = type;
    // }
    private PaymentProperties paymentProperties;
    public PaymentGateway(PaymentProperties paymentProperties){
        this.paymentProperties=paymentProperties;
    }

    // public String getType() {
    //     return type;
    // }
    // public void setType(String type) {
    //     this.type = type;
    // }
    // public int getRetryCount() {
    //     return retryCount;
    // }
    // public void setRetryCount(int retryCount) {
    //     this.retryCount = retryCount;
    // }

    public String getType() {
        return paymentProperties.getType();
    }

    

    public int getRetryCount() {
        return paymentProperties.getRetryCount();
    }

    

    public int getTimeout() {
        return paymentProperties.getTimeout();
    }

    

    public boolean isEnabled() {
        return paymentProperties.isEnabled();
    }

    public void print(){
        System.out.println(getType());
		System.out.println(getRetryCount());
		System.out.println(isEnabled());
		System.out.println(getTimeout());
    }
    
    
}
