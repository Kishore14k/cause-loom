package com.kishore.causeloom;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class CauseloomApplication {

	public static void main(String[] args) {
		SpringApplication.run(CauseloomApplication.class, args);
	}

}
