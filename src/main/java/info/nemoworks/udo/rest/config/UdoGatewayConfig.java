package info.nemoworks.udo.rest.config;

import com.google.common.eventbus.EventBus;
import info.nemoworks.udo.messaging.gateway.HTTPServiceGateway;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UdoGatewayConfig {
//    @Bean
//    public HTTPServiceGateway httpServiceGateway(){
//        return new HTTPServiceGateway();
//    }

    @Bean
    public EventBus eventBus() {
        return new EventBus();
    }


}
