package com.qbot.cq.business.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;

@Slf4j
@SpringBootApplication
public class UserApplication {
    public static void main(String[] args) {
        ConfigurableApplicationContext configurableApplicationContext = SpringApplication.run(UserApplication.class, args);
        Environment environment = configurableApplicationContext.getBean(Environment.class);
        log.info(" >>> Start successful，Access link: http://localhost:{}",environment.getProperty("server.port"));
    }
}
