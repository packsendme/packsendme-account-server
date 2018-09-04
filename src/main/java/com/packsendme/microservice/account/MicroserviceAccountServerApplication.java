package com.packsendme.microservice.account;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
@EnableEurekaClient
public class MicroserviceAccountServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(MicroserviceAccountServerApplication.class, args);
	}
	
}
