package net.accel_tech.cloud_server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;

@SpringBootApplication
@EnableConfigServer
public class CloudServerApplication {

	private static final Logger LOGGER = LoggerFactory.getLogger(CloudServerApplication.class);

	public static void main(String[] args) {

		SpringApplication.run(CloudServerApplication.class, args);

   		LOGGER.info("Cloud Server started successfully...");
	}

}
