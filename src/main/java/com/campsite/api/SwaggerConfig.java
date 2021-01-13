package com.campsite.api;

import java.util.Collections;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

@Configuration
public class SwaggerConfig {

	@Bean
	public Docket api() {
		return new Docket(DocumentationType.SWAGGER_2)
				.select()
				.apis(RequestHandlerSelectors.basePackage("com.campsite.api"))
				.paths(PathSelectors.ant("/api/*"))
				.build();
	}

	private ApiInfo apiInfo() {
		return new ApiInfo(
			"Campsite Reservations Api",
			"Api Service to manage campsite Reservations. Reservations can be booked from 1-3 days.",
			"API TOS",
			"Terms Of Service",
			new Contact("Kalai Mahent", "www.campsite-pacific.com", "kalai@campsite-pacific.com"),
			"Licence by Campsite Reservations", "www.campsite-pacific.com/api/license",
			Collections.emptyList());
	}
	
}
