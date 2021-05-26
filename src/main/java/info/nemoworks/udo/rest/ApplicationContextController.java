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

import java.util.List;
import java.util.Set;
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
        return applicationContext.getAppId();
    }

    @DeleteMapping("/applicationContext")
    public Set<String> deleteApplicationContext(@RequestParam String id) throws MqttException {
        ApplicationContextCluster.removeApplicationContext(id);
        return ApplicationContextCluster.getApplicationContextMap().keySet();
    }



}
