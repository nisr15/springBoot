package com.example.beanlifecycle;

import org.springframework.beans.factory.BeanNameAware;
import org.springframework.stereotype.Component;

@Component
public class UserService {

    public UserService(){
        System.out.println("UserService created");
    }

}
