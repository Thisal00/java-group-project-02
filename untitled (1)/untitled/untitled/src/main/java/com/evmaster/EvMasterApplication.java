package com.evmaster;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = {
        "com.evmaster.repository",
        "com.evmaster.community.repository"
})
public class EvMasterApplication {

    public static void main(String[] args) {
        SpringApplication.run(EvMasterApplication.class, args);
    }

}
