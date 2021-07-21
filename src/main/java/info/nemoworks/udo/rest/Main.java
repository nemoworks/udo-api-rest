package info.nemoworks.udo.rest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "info.nemoworks.udo")
public class Main {

    public static void main(String[] args) {
        SpringApplication.run(UdoRestApplication.class, args);
    }
}
