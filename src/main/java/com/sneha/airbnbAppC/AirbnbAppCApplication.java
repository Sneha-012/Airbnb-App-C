package com.sneha.airbnbAppC;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AirbnbAppCApplication {

	public static void main(String[] args) {
		SpringApplication.run(AirbnbAppCApplication.class, args);
	}

}
