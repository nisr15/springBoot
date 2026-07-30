package com.example;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration  // Telling it is a config file
@ComponentScan("com.example")  // scans which classes has @component , which should it handle
public class AppConfig {
    //empty
}
