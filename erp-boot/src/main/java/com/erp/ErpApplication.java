package com.erp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ErpApplication {

    public static void main(String[] args) {

        SpringApplication.run(ErpApplication.class, args);
        System.out.println("http://127.0.0.1:8082/swagger-ui.html");
        System.out.println("http://127.0.0.1:8082/v3/api-docs");
    }

}
