package com.application.cabapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;

// Authentication is JWT-based (see SecurityConfig), so Spring Boot's default
// in-memory user with a generated password is not needed.
@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
public class CabappApplication {

	public static void main(String[] args) {
		SpringApplication.run(CabappApplication.class, args);
	}

}
