package info.nemoworks.udo.rest;

import info.nemoworks.udo.messaging.gateway.UdoGateway;
import info.nemoworks.udo.messaging.messaging.ApplicationContext;
import info.nemoworks.udo.messaging.messaging.ApplicationContextCluster;
import info.nemoworks.udo.messaging.messaging.Publisher;
import info.nemoworks.udo.messaging.messaging.Subscriber;
import info.nemoworks.udo.model.Udo;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.javatuples.Pair;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@CrossOrigin
@RequestMapping("/api")
public class ApplicationContextController {
    private final Publisher publisher;

    private final Subscriber subscriber;

    private final UdoGateway udoGateway;

    public ApplicationContextController(Publisher publisher, Subscriber subscriber, UdoGateway udoGateway) {
        this.publisher = publisher;
        this.subscriber = subscriber;
        this.udoGateway = udoGateway;
    }

    @PostMapping("/applicationContext")
    public String createApplicationContext(@RequestParam String id) throws MqttException {
        ApplicationContext applicationContext = new ApplicationContext(publisher,subscriber,udoGateway,id);
        Udo udo = new Udo(null, null);
        udo.setId(UUID.randomUUID().toString());
        Pair<String, String> mqttTopic = applicationContext.getMqttTopic(applicationContext.getAppId(), udo);
        applicationContext.subscribeMessage(applicationContext.getAppId(), udo);
        applicationContext.publishMessage(mqttTopic.getValue1(), "asasasaxcasdcswd".getBytes());

        System.out.println("num of applicationContext: "+ApplicationContextCluster.getApplicationContextMap().size());
        return applicationContext.getAppId();
    }
}
