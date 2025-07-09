package com.example.equationservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.example.equation", "com.example.equationservice"})
public class EquationServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(EquationServiceApplication.class, args);
	}

}
