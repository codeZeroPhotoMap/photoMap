package com.codeZero.photoMap;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class PhotoMapApplication {

	public static void main(String[] args) {
		SpringApplication.run(PhotoMapApplication.class, args);
	}

}
