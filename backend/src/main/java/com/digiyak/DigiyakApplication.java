package com.digiyak;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.modulith.Modulithic;

@Modulithic(
        systemName = "Digiyak Campus Walking Delivery Network",
        sharedModules = "shared"
)
@SpringBootApplication
public class DigiyakApplication {

    public static void main(String[] args) {
        SpringApplication.run(DigiyakApplication.class, args);
    }
}
