package info.nemoworks.udo.rest;

import com.google.common.eventbus.EventBus;
import info.nemoworks.udo.messaging.HTTPServiceGateway;
import info.nemoworks.udo.service.SaveByUriEventHandler;
import info.nemoworks.udo.service.SyncEventHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import javax.annotation.PostConstruct;

@SpringBootApplication
@ComponentScan(basePackages = "info.nemoworks.udo")
@Slf4j
public class UdoRestApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(UdoRestApplication.class, args);
    }


    @Autowired
    private HTTPServiceGateway httpServiceGateway;

    @Autowired
    EventBus eventBus;

    @Autowired
    SyncEventHandler syncEventHandler;

    @Autowired
    SaveByUriEventHandler saveByUriEventHandler;

    @PostConstruct
    public void registerEventHandler() {
        eventBus.register(httpServiceGateway);
        eventBus.register(syncEventHandler);
        eventBus.register(saveByUriEventHandler);
    }


    @Override
    public void run(String... args) throws Exception {

        while (true) {
            System.out.println("start...");
            if (httpServiceGateway.getEndpoints().size() > 0) {
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
