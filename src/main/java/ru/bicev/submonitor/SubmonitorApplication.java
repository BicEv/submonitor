package ru.bicev.submonitor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class SubmonitorApplication {

	public static void main(String[] args) {
		SpringApplication.run(SubmonitorApplication.class, args);
	}

}
