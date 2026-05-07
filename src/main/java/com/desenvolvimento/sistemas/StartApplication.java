package com.desenvolvimento.sistemas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


import com.desenvolvimento.sistemas.model.domain.factory.SchemaValidator;

@SpringBootApplication
public class StartApplication {

	public static void main(String[] args) {
		// Roda a verificação automática antes de qualquer outra coisa
		SchemaValidator.validateAllEntities();
	 // Este comando inicia o servidor Tomcat embutido e sobe sua API
        SpringApplication.run(StartApplication.class, args);
        System.out.println("=== Servidor Spring Boot iniciado com sucesso! ===");
    }
}