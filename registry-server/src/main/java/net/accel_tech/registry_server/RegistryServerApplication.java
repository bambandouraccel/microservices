package net.accel_tech.registry_server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@SpringBootApplication
@EnableEurekaServer
public class RegistryServerApplication {

	public static void main(String[] args) {

		SpringApplication.run(RegistryServerApplication.class, args);

		System.out.println("Eureka Server Started...");
	}

}
