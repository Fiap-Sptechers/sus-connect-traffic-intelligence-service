package com.fiap.sus.traffic.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configuração do Swagger/OpenAPI para documentação da API.
 * 
 * A documentação estará disponível em:
 * - Swagger UI: http://localhost:8082/swagger-ui.html
 * - OpenAPI JSON: http://localhost:8082/v3/api-docs
 * - OpenAPI YAML: http://localhost:8082/v3/api-docs.yaml
 */
@Configuration
public class OpenApiConfig {

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
        Server localServer = new Server();
        localServer.setUrl("http://localhost:8082");
        localServer.setDescription("Servidor Local");

        Server cloudServer = new Server();
        cloudServer.setUrl("https://traffic-intelligence-service-{environment}.run.app");
        cloudServer.setDescription("Cloud Run (produção)");

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
                .servers(List.of(localServer, cloudServer));
    }
}
