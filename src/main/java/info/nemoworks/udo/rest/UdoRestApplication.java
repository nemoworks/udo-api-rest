package info.nemoworks.udo.rest;

import java.util.Arrays;

import com.google.common.eventbus.EventBus;

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
public class UdoRestApplication {

	public static void main(String[] args) {
		SpringApplication.run(UdoRestApplication.class, args);
	}

	@Bean
	public EventBus eventBus() {
		return new EventBus();
	}

	@Bean
	public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
		return args -> {

			log.info("Let's inspect the beans provided by Spring Boot:");

			String[] beanNames = ctx.getBeanDefinitionNames();
			Arrays.sort(beanNames);
			for (String beanName : beanNames) {
				log.info(beanName);
			}
		};
	}

}
