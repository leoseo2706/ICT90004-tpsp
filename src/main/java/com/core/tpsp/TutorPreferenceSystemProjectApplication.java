package com.core.tpsp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages={"com.core.tpsp"})
//@EnableTransactionManagement
//@EntityScan(basePackages="com.core.tpsp.entity")
//@EnableJpaRepositories(basePackages="com.core.tpsp.repo")
//remove when adding db driver
@EnableAutoConfiguration(exclude={DataSourceAutoConfiguration.class,HibernateJpaAutoConfiguration.class})
public class TutorPreferenceSystemProjectApplication {

	public static void main(String[] args) {
		SpringApplication.run(TutorPreferenceSystemProjectApplication.class, args);
	}

}
