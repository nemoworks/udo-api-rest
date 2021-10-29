package info.nemoworks.udo.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.eventbus.EventBus;
import info.nemoworks.udo.messaging.gateway.HTTPServiceGateway;
import info.nemoworks.udo.messaging.gateway.MQTTGateway;
import info.nemoworks.udo.messaging.messaging.ApplicationContext;
import info.nemoworks.udo.messaging.messaging.ApplicationContextCluster;
import info.nemoworks.udo.messaging.messaging.FilterRule;
import info.nemoworks.udo.messaging.messaging.Publisher;
import info.nemoworks.udo.messaging.messaging.Subscriber;
import info.nemoworks.udo.model.Udo;
import info.nemoworks.udo.service.UdoService;
import java.io.IOException;
import java.util.Set;
import java.util.UUID;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin
@RequestMapping("/api")
public class ApplicationContextController {

    @Autowired
    UdoService udoService;

    @Autowired
    EventBus eventBus;

    @Autowired
    HTTPServiceGateway httpServiceGateway;

    @Autowired
    MQTTGateway mqttGateway;


    @PostMapping("/applicationContext")
    public String createApplicationContext(@RequestParam String id)
        throws MqttException, IOException {
        String clientid1 = UUID.randomUUID().toString();
        MqttClient client1 = new MqttClient("tcp://210.28.132.168:30609", clientid1);
        MqttConnectOptions options = new MqttConnectOptions();
        options.setUserName("udo-user");
        char[] password = "123456".toCharArray();
        options.setPassword(password);
        options.setAutomaticReconnect(true);
        options.setCleanSession(true);
        options.setMqttVersion(MqttConnectOptions.MQTT_VERSION_DEFAULT);
        options.setConnectionTimeout(10);
        client1.connect(options);
        Publisher httpPublisher = new Publisher(client1);

        String clientid2 = UUID.randomUUID().toString();
        MqttClient client2 = new MqttClient("tcp://210.28.132.168:30609", clientid2);
        client2.connect(options);
        Subscriber httpSubscriber = new Subscriber(client2);

        String clientid3 = UUID.randomUUID().toString();
        MqttClient client3 = new MqttClient("tcp://210.28.132.168:30609", clientid3);
        String clientid4 = UUID.randomUUID().toString();
        MqttClient client4 = new MqttClient("tcp://210.28.132.168:30609", clientid4);

        client3.connect(options);
        client4.connect(options);
        Publisher mqttPublisher = new Publisher(client3);
        Subscriber mqttSubscriber = new Subscriber(client4);
        ApplicationContext applicationContext = new ApplicationContext(httpPublisher,
            httpSubscriber,
            mqttPublisher, mqttSubscriber,
            httpServiceGateway, mqttGateway, udoService);
        applicationContext.setAppId(id);
        eventBus.register(applicationContext);
        applicationContext.subscribeMessage(applicationContext.getAppId());
        return applicationContext.getAppId();
    }

    @GetMapping("/applicationContext")
    public Set<String> getApplicationContext() {
        return ApplicationContextCluster.getApplicationContextMap().keySet();
    }

    @GetMapping("/applicationContext/{applicationContextId}")
    public Set<String> getApplicationContextUdos(@PathVariable String applicationContextId) {
        return ApplicationContextCluster.getApplicationContextMap()
            .get(applicationContextId).getValue1();
    }

    @PostMapping("/applicationContext/{applicationContextId}")
    public void addUdoInApplicationContext(@PathVariable String applicationContextId,
        @RequestParam String udoId) throws JsonProcessingException, MqttException {
        Udo udo = udoService.getUdoById(udoId);
        ApplicationContext applicationContext = ApplicationContextCluster.getApplicationContextMap()
            .get(applicationContextId).getValue0();
        ApplicationContextCluster.getApplicationContextMap().get(applicationContextId)
            .getValue1().add(udo.getId());
        applicationContext.publishRegisterMessage(applicationContext.getAppId(), udo.getId());
//        applicationContext.subscribeMessage(applicationContext.getAppId(), udo);
    }


    @DeleteMapping("/applicationContext/{applicationContextId}")
    public void deleteUdoInApplicationContext(@PathVariable String applicationContextId,
        @RequestParam String udoId) throws JsonProcessingException, MqttException {
        Udo udo = udoService.getUdoById(udoId);
        ApplicationContextCluster.getApplicationContextMap().get(applicationContextId)
            .getValue1().remove(udo.getId());
        ApplicationContext applicationContext = ApplicationContextCluster.getApplicationContextMap()
            .get(applicationContextId).getValue0();
        applicationContext.publishDeleteMessage(applicationContext.getAppId(), udo.getId());
    }

    @DeleteMapping("/applicationContext")
    public Set<String> deleteApplicationContext(@RequestParam String id) throws MqttException {
        ApplicationContextCluster.removeApplicationContext(id);
        return ApplicationContextCluster.getApplicationContextMap().keySet();
    }

    @PostMapping("/applicationContext/filter")
    public String setFilterRule(@RequestBody String filterRule, @RequestParam String id,
        @RequestParam String uid) {
        System.out.println("filterRule: " + filterRule);
        ApplicationContextCluster.getApplicationContextMap().get(id)
            .getValue0()
            .addFilterRule(uid, new FilterRule(filterRule));
        return filterRule;
    }

    @GetMapping("/applicationContext/filter")
    public String getFilterRule(@RequestParam String id, @RequestParam String uid) {
        return ApplicationContextCluster.getApplicationContextMap().get(id)
            .getValue0()
            .getFilterRule(uid)
            .getFilterString();
    }
}
