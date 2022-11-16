package io.mo.coronatrackapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CoronaTrackAppApplication {

    public static void main(String[] args) {
        SpringApplication.run(CoronaTrackAppApplication.class, args);
    }

}
