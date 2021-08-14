package com.myproject.mediumclone;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})
public class MediumCloneApplication {

    public static void main(String[] args) {
        SpringApplication.run(MediumCloneApplication.class, args);
    }

}
