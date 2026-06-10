package com.application.cabapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CabappApplication {

	public static void main(String[] args) {
		SpringApplication.run(CabappApplication.class, args);
		System.out.println("Application started successfully on port 8080");
	}

}
