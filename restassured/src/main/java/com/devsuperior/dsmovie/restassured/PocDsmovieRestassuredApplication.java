package com.devsuperior.dsmovie.restassured;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.devsuperior.dsmovie.restassured.repositories")
@EntityScan(basePackages = "com.devsuperior.dsmovie.restassured.entities")
public class PocDsmovieRestassuredApplication {
    public static void main(String[] args) {
        SpringApplication.run(PocDsmovieRestassuredApplication.class, args);
    }
}
