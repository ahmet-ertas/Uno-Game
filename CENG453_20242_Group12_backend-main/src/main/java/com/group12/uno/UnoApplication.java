package com.group12.uno;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@ComponentScan(basePackages = "com.group12.uno")
public class UnoApplication {
	public static void main(String[] args) {
		SpringApplication.run(UnoApplication.class, args);
	}
}

@RestController
@RequestMapping("/api")
class HelloWorldController {
	@GetMapping("/hello")
	public String sayHello() {
		return "Hello, World!";
	}
}
