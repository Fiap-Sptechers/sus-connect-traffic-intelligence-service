package com.fiap.sus.traffic.infrastructure.config;

import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TomcatConfig {

    @Bean
    public ServletWebServerFactory servletContainer() {
        TomcatServletWebServerFactory factory = new TomcatServletWebServerFactory();
        factory.addConnectorCustomizers(connector -> {
            connector.setProperty("relaxedQueryChars", "<>[\\]^`{|}");
            connector.setProperty("relaxedPathChars", "<>[\\]^`{|}");
            
            connector.setURIEncoding("UTF-8");
        });
        return factory;
    }
}
