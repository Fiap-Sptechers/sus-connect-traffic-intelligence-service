package com.fiap.sus.traffic.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuração do Swagger/OpenAPI para documentação da API.
 */
@Configuration
public class OpenApiConfig {

    @Value("${swagger.server.url:${SWAGGER_SERVER_URL:}}")
    private String swaggerServerUrl;

    @Value("${server.port:8082}")
    private String serverPort;

    @Value("${spring.profiles.active:}")
    private String activeProfile;

    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("traffic-intelligence-api")
                .pathsToMatch("/direcionamento/**")
                .packagesToScan("com.fiap.sus.traffic.presentation.controller")
                .build();
    }

    @Bean
    public OpenAPI trafficIntelligenceOpenAPI() {
        List<Server> servers = new ArrayList<>();

        if (isProductionEnvironment()) {
            Server productionServer = new Server();
            productionServer.setUrl("/");
            productionServer.setDescription("Produção (detectado automaticamente)");
            servers.add(productionServer);
            
            if (swaggerServerUrl != null && !swaggerServerUrl.isBlank()) {
                Server explicitServer = new Server();
                explicitServer.setUrl(swaggerServerUrl);
                explicitServer.setDescription("Produção (configurado)");
                servers.add(explicitServer);
            }
        } else {
            Server localServer = new Server();
            localServer.setUrl("http://localhost:" + serverPort);
            localServer.setDescription("Servidor Local");
            servers.add(localServer);
            
            if (swaggerServerUrl != null && !swaggerServerUrl.isBlank()) {
                Server productionServer = new Server();
                productionServer.setUrl(swaggerServerUrl);
                productionServer.setDescription("Produção");
                servers.add(productionServer);
            }
        }


        Contact contact = new Contact();
        contact.setName("SusConnect Team");
        contact.setEmail("susconnect@fiap.com.br");
        contact.setUrl("https://github.com/fiap/sus-connect");

        License license = new License()
                .name("Apache 2.0")
                .url("https://www.apache.org/licenses/LICENSE-2.0.html");

        Info info = new Info()
                .title("SusConnect Traffic Intelligence Service API")
                .version("1.0.0")
                .description("""
                    Motor analítico de direcionamento de pacientes para unidades de saúde.
                    
                    Este serviço utiliza algoritmos de inteligência para calcular a melhor unidade de saúde
                    para direcionar um paciente baseado em:
                    - Distância geográfica
                    - Tempo médio de atendimento (TMA)
                    - Ocupação da unidade
                    - Especialidade médica disponível
                    - Classificação de risco do paciente (Protocolo Manchester)
                    
                    ## Funcionalidades
                    - Consulta de direcionamento inteligente
                    - Cache de resultados para melhor performance
                    - Integração com Network Service e LiveOps Service
                    - Métricas e observabilidade via Actuator
                    """)
                .contact(contact)
                .license(license);

        return new OpenAPI()
                .info(info)
                .servers(servers);
    }

    /**
     * Verifica se está em ambiente de produção (Cloud Run).
     * Detecta via:
     * 1. Profile ativo = "cloud"
     * 2. Variável de ambiente K_SERVICE (Cloud Run sempre define)
     * 3. Variável de ambiente PORT (Cloud Run sempre define, diferente de 8082)
     */
    private boolean isProductionEnvironment() {
        if ("cloud".equals(activeProfile)) {
            return true;
        }
        
        String kService = System.getenv("K_SERVICE");
        String port = System.getenv("PORT");
        
        if (kService != null && !kService.isBlank()) {
            return true;
        }
        
        if (port != null && !port.equals(serverPort) && !port.equals("8082")) {
            return true;
        }
        
        return false;
    }
}
