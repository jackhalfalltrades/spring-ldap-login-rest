package com.maat.bestbuy.integration;

import com.ulisesbocchio.jasyptspringboot.annotation.EnableEncryptableProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportResource;

//@EnableDiscoveryClient
@EnableEncryptableProperties
@SpringBootApplication
@ImportResource("classpath:app-config.xml")
public class AdministratorLogin {

	public static void main(String[] args) {
		SpringApplication.run(AdministratorLogin.class, args);
	}
}
