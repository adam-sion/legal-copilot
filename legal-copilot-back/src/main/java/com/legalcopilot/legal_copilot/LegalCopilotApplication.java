package com.legalcopilot.legal_copilot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class LegalCopilotApplication {

	public static void main(String[] args) {
		SpringApplication.run(LegalCopilotApplication.class, args);
	}

}
