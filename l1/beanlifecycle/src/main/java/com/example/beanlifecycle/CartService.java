package com.example.beanlifecycle;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Component
public class CartService /*implements InitializingBean*/ {  //used when you want to do something After creating bean but before bussiness logic

    Map<Integer,String> mp;

    public CartService(){
        mp=new HashMap<>();
        System.out.println("Cart Constructor called");
    }

    public void addToCart(){
        System.out.println("Added to cart");
    }

    // @Override
    // public void afterPropertiesSet() throws Exception {
    //     mp.put(1,"ISR");
    //     mp.put(2,"NISR");
    //     System.out.println("Intialising is done");
    // }

    // public void start(){
    //     mp.put(1,"ISR");
    //     mp.put(2,"NISR");
    //     System.out.println("Intialising is done");
    // }


    @PostConstruct
    public void start2(){
        mp.put(1,"ISR");
        mp.put(2,"NISR");
        System.out.println("Intialising is done");
    }

    public String getName(int key){
        return mp.get(key);
    }
}
