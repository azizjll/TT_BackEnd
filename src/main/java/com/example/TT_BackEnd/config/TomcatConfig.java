package com.example.TT_BackEnd.config;

import org.apache.coyote.http11.Http11NioProtocol;
import org.springframework.boot.tomcat.servlet.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TomcatConfig {

    @Bean
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> tomcatCustomizer() {
        return factory -> factory.addConnectorCustomizers(connector -> {
            // Augmente la limite des parts multipart directement sur Tomcat
            var tomcat = (org.apache.catalina.connector.Connector) connector;
            tomcat.setMaxParameterCount(500);

            // Accède au protocol handler pour les limits multipart
            if (tomcat.getProtocolHandler() instanceof Http11NioProtocol proto) {
                proto.setMaxKeepAliveRequests(500);
            }
        });
    }
}