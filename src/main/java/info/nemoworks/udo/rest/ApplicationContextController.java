package info.nemoworks.udo.rest;

import com.google.common.eventbus.EventBus;
import info.nemoworks.udo.messaging.gateway.HTTPServiceGateway;
import info.nemoworks.udo.messaging.messaging.ApplicationContext;
import info.nemoworks.udo.messaging.messaging.ApplicationContextCluster;
import info.nemoworks.udo.messaging.messaging.Publisher;
import info.nemoworks.udo.messaging.messaging.Subscriber;
import info.nemoworks.udo.model.Udo;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Set;
import java.util.UUID;

@RestController
@CrossOrigin
@RequestMapping("/api")
public class ApplicationContextController {
    private final Publisher publisher;

    private final Subscriber subscriber;

    @Autowired
    EventBus eventBus;

    @Autowired
    HTTPServiceGateway httpServiceGateway;

    public ApplicationContextController(Publisher publisher, Subscriber subscriber) {
        this.publisher = publisher;
        this.subscriber = subscriber;
    }

    @PostMapping("/applicationContext")
    public String createApplicationContext(@RequestParam String id) throws MqttException {
        ApplicationContext applicationContext = new ApplicationContext(publisher,subscriber, httpServiceGateway);
        applicationContext.setAppId(id);
        eventBus.register(applicationContext);
        Udo udo = new Udo(null, null);
        udo.setId("udo");
        applicationContext.subscribeMessage(applicationContext.getAppId(),udo);
        return applicationContext.getAppId();
    }

    @DeleteMapping("/applicationContext")
    public Set<String> deleteApplicationContext(@RequestParam String id) throws MqttException {
        ApplicationContextCluster.removeApplicationContext(id);
        return ApplicationContextCluster.getApplicationContextMap().keySet();
    }



}
