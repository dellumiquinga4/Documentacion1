package com.banquito.Documentacion;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableMongoAuditing
@EnableFeignClients(basePackages = "com.banquito.Documentacion.client")
public class DocumentacionApplication {

	public static void main(String[] args) {
		SpringApplication.run(DocumentacionApplication.class, args);
	}

}
