package info.nemoworks.udo.rest;

import com.google.common.eventbus.EventBus;
import info.nemoworks.udo.messaging.gateway.HTTPServiceGateway;
import info.nemoworks.udo.messaging.gateway.MQTTGateway;
import info.nemoworks.udo.service.eventHandler.SaveByUriEventHandler;
import info.nemoworks.udo.service.eventHandler.SubscribeByMqttEventHandler;
import info.nemoworks.udo.service.eventHandler.SyncEventHandler;
import java.net.URISyntaxException;
import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.ComponentScan;


@ComponentScan(basePackages = "info.nemoworks.udo")
@Slf4j
public class UdoRestApplication implements CommandLineRunner {


    @Autowired
    private HTTPServiceGateway httpServiceGateway;

    @Autowired
    private MQTTGateway mqttGateway;

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
        eventBus.register(mqttGateway);
        eventBus.register(httpServiceGateway);
        eventBus.register(syncEventHandler);
        eventBus.register(saveByUriEventHandler);
        eventBus.register(subscribeByMqttEventHandler);
//        httpServiceGateway.register("OfCKE3oBtyoKFg71_rOW", new URI("http://localhost:8999/air"));
//        httpServiceGateway.register("N_CKE3oBtyoKFg71Q7PL", new URI("http://localhost:8998/airquality"));
    }


    @Override
    public void run(String... args) throws Exception {

//        while (true) {
//            System.out.println("start...");
        if (httpServiceGateway.getEndpoints().size() > 0) {
            httpServiceGateway.start();
        }
        if (mqttGateway.getEndpoints().size() > 0) {
            mqttGateway.start();
        }
        if (httpServiceGateway.getEndpoints().size() > 0) {
//                mqttGateway.getEndpoints().size() > 0) {
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
//            }
//        }
        }
    }

}
