package info.nemoworks.udo.rest;

import java.util.Arrays;

import com.google.common.eventbus.EventBus;

import info.nemoworks.udo.messaging.HTTPServiceGateway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

import lombok.extern.slf4j.Slf4j;

@SpringBootApplication
@ComponentScan(basePackages = "info.nemoworks.udo")
@Slf4j
public class UdoRestApplication implements CommandLineRunner{

	public static void main(String[] args) {
		SpringApplication.run(UdoRestApplication.class, args);
	}


	@Autowired
    private HTTPServiceGateway httpServiceGateway;

	@Autowired
	EventBus eventBus;


    @Override
    public void run(String... args) throws Exception {
		eventBus.register(httpServiceGateway);
        while(true){
            System.out.println("start...");
            if(httpServiceGateway.getEndpoints().size()>0){
                httpServiceGateway.start();
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}
