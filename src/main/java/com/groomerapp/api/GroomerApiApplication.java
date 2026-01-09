package com.groomerapp.api;

import com.groomerapp.api.shared.config.AppJwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableConfigurationProperties(AppJwtProperties.class)
@ConfigurationPropertiesScan
@SpringBootApplication
public class GroomerApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(GroomerApiApplication.class, args);
	}

}
