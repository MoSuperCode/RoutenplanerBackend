package org.example.tourplannerbackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.http.client.HttpClientAutoConfiguration;

@SpringBootApplication(exclude = {HttpClientAutoConfiguration.class})
public class TourplannerBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(TourplannerBackendApplication.class, args);
    }
}
