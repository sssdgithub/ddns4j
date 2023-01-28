package com.sssd.alidns;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author SSSD
 */
@SpringBootApplication
@EnableScheduling
public class AlidnsApplication {

    public static void main(String[] args) {
        SpringApplication.run(AlidnsApplication.class, args);
    }

}
