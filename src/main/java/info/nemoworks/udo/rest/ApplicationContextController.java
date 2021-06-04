package info.nemoworks.udo.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.eventbus.EventBus;
import com.google.gson.JsonObject;
import info.nemoworks.udo.messaging.gateway.HTTPServiceGateway;
import info.nemoworks.udo.messaging.messaging.ApplicationContext;
import info.nemoworks.udo.messaging.messaging.ApplicationContextCluster;
import info.nemoworks.udo.messaging.messaging.Publisher;
import info.nemoworks.udo.messaging.messaging.Subscriber;
import info.nemoworks.udo.model.Udo;
import info.nemoworks.udo.model.UdoType;
import info.nemoworks.udo.service.UdoService;
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
    UdoService udoService;

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

    @PostMapping("/applicationContext/{applicationContextId}")
    public void addUdoInApplicationContext(@PathVariable String applicationContextId,@RequestParam String udoId) throws JsonProcessingException, MqttException {
        Udo udo = udoService.getUdoById(udoId);
        ApplicationContext applicationContext = ApplicationContextCluster.getApplicationContextMap()
                .get(applicationContextId).getValue0();
        ApplicationContextCluster.getApplicationContextMap().get(applicationContextId)
                .getValue1().add(udo.getId());
        applicationContext.subscribeMessage(applicationContext.getAppId(),udo);
    }

    @DeleteMapping("/applicationContext")
    public Set<String> deleteApplicationContext(@RequestParam String id) throws MqttException {
        ApplicationContextCluster.removeApplicationContext(id);
        return ApplicationContextCluster.getApplicationContextMap().keySet();
    }



}
