package com.core.tpsp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
@EntityScan(basePackages = "com.core.tpsp.entity")
@EnableJpaRepositories(basePackages = "com.core.tpsp.repo")
public class TutorPreferenceSystemProjectApplication {

    public static void main(String[] args) {
        SpringApplication.run(TutorPreferenceSystemProjectApplication.class, args);
    }

}
