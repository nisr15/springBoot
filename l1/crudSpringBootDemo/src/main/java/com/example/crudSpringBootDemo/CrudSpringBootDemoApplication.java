package com.example.crudSpringBootDemo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// @SpringBootApplication(exclude={DataSourceAutoConfiguration.class})
@SpringBootApplication
public class CrudSpringBootDemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(CrudSpringBootDemoApplication.class, args);

		// System.out.println("Hello world");

		
	}

}
