package com.example;

import org.springframework.stereotype.Component;

// @Component 
// Here @Component it will not work as spring ioc container cannot create the object for this as it needs two parameters
// same issue we face with the class which are in external jars for this we use @Bean annotation, applied on a method (not a class) inside a @Configuration class
public class User {
    private String name;
    private int age;

    public User(String name, int age) {
        this.name = name;
        this.age = age;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public int getAge() {
        return age;
    }
    public void setAge(int age) {
        this.age = age;
    }

    
}
