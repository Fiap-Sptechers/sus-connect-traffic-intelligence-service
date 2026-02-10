package com.fiap.sus.traffic.infrastructure.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
@ConfigurationProperties(prefix = "traffic.intelligence")
@Getter
@Setter
public class TrafficIntelligenceProperties {

    private NetworkService networkService = new NetworkService();
    private LiveOpsService liveopsService = new LiveOpsService();
    private Cache cache = new Cache();
    private Algoritmo algoritmo = new Algoritmo();

    @Getter
    @Setter
    public static class NetworkService {
        private String url = "http://localhost:8080";
        private int timeout = 180000;  // 3 minutos - necessário para processar grandes volumes (596k+ unidades)
        private int connectTimeout = 5000;
    }

    @Getter
    @Setter
    public static class LiveOpsService {
        private String url = "http://localhost:8081";
        private int timeout = 2000;
        private int connectTimeout = 1000;
    }

    @Getter
    @Setter
    public static class Cache {
        private Duration ttlIndicadores = Duration.ofSeconds(30);
        private Duration ttlUnidades = Duration.ofSeconds(60);
        private Duration ttlPesos = Duration.ofSeconds(300);
        private Duration ttlSugestoes = Duration.ofSeconds(300);  // 5 minutos
    }

    @Getter
    @Setter
    public static class Algoritmo {
        private Pesos pesos = new Pesos();
        private int maxSugestoes = 5;
        private double raioDefaultKm = 50.0;
        private double raioMinimoKm = 1.0;
        private double raioMaximoKm = 100.0;

        @Getter
        @Setter
        public static class Pesos {
            private double distancia = 0.3;
            private double tma = 0.4;
            private double ocupacao = 0.2;
            private double especialidade = 0.1;
        }
    }
}
