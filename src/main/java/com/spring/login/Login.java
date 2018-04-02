package com.spring.login;

import com.ulisesbocchio.jasyptspringboot.annotation.EnableEncryptableProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportResource;

//@EnableDiscoveryClient
@EnableEncryptableProperties
@SpringBootApplication
@ImportResource("classpath:app-config.xml")
public class Login {

    public static void main(String[] args) {
        SpringApplication.run(Login.class, args);
    }
}
