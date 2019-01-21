package com.feri.selfcoding_videoprovider;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.ImportResource;

@SpringBootApplication
@MapperScan("com.feri.dao")
@ImportResource("classpath:dubboprovider.xml")
public class SelfcodingVideoproviderApplication {

    public static void main(String[] args) {
        SpringApplication.run(SelfcodingVideoproviderApplication.class, args);
    }

}

