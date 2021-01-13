package com.campsite.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import(value = { SwaggerConfig.class })
public class CampsiteApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(CampsiteApiApplication.class, args);
	}
	
}
