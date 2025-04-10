package com.example.hubeiatlasbackend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.example.hubeiatlasbackend.mapper")
public class HubeiAtlasBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(HubeiAtlasBackendApplication.class, args);
    }

}
