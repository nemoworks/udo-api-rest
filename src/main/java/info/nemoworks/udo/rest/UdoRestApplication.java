package info.nemoworks.udo.rest;

import com.google.common.eventbus.EventBus;
import info.nemoworks.udo.messaging.gateway.HTTPServiceGateway;
import info.nemoworks.udo.service.eventHandler.SaveByUriEventHandler;
import info.nemoworks.udo.service.eventHandler.SubscribeByMqttEventHandler;
import info.nemoworks.udo.service.eventHandler.SyncEventHandler;
import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import java.net.URI;
import java.net.URISyntaxException;

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

    @Autowired
    SubscribeByMqttEventHandler subscribeByMqttEventHandler;

    @PostConstruct
    public void registerEventHandler() throws URISyntaxException {
        eventBus.register(httpServiceGateway);
        eventBus.register(syncEventHandler);
        eventBus.register(saveByUriEventHandler);
        eventBus.register(subscribeByMqttEventHandler);
        httpServiceGateway.register("OfCKE3oBtyoKFg71_rOW", new URI("http://localhost:8999/air"));
        httpServiceGateway.register("N_CKE3oBtyoKFg71Q7PL", new URI("http://localhost:8998/airquality"));
    }


    @Override
    public void run(String... args) throws Exception {

        while (true) {
//            System.out.println("start...");
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
