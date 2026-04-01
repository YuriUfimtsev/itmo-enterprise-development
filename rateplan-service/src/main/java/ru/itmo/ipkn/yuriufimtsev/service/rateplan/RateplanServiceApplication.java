package ru.itmo.ipkn.yuriufimtsev.service.rateplan;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class RateplanServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(RateplanServiceApplication.class, args);
    }
}
