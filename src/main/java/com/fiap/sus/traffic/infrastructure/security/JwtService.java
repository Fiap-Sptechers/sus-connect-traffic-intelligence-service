package com.fiap.sus.traffic.infrastructure.security;

import com.fiap.sus.traffic.infrastructure.config.TrafficIntelligenceProperties;
import io.jsonwebtoken.Jwts;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class JwtService {

    private final TrafficIntelligenceProperties properties;
    private PrivateKey privateKey;
    private static final String ISSUER = "sus-connect-traffic-api";
    private static final String AUDIENCE = "sus-connect-liveops-api";
    private static final long TOKEN_VALIDITY_MS = 300000;

    @PostConstruct
    public void init() {
        String privateKeyPem = properties.getLiveopsService().getPrivateKey();
        
        if (privateKeyPem == null) {
            log.error("TRAFFIC_LIVEOPS_PRIVATE_KEY is null");
            this.privateKey = null;
        } else if (privateKeyPem.isBlank()) {
            log.error("TRAFFIC_LIVEOPS_PRIVATE_KEY is blank");
            this.privateKey = null;
        } else {
            this.privateKey = parsePrivateKey(privateKeyPem);
        }
    }

    public String generateToken() {
        if (privateKey == null) {
            init();
            if (privateKey == null) {
                return null;
            }
        }

        try {
            Date now = new Date();
            Date expiration = new Date(now.getTime() + TOKEN_VALIDITY_MS);

            return Jwts.builder()
                    .issuer(ISSUER)
                    .audience().add(AUDIENCE).and()
                    .subject("traffic-intelligence-service")
                    .issuedAt(now)
                    .expiration(expiration)
                    .id(UUID.randomUUID().toString())
                    .claim("service", "traffic-intelligence")
                    .signWith(privateKey)
                    .compact();
            
        } catch (Exception e) {
            log.error("Error generating JWT: {}", e.getMessage(), e);
            return null;
        }
    }

    private PrivateKey parsePrivateKey(String privateKeyPem) {
        if (privateKeyPem == null || privateKeyPem.isBlank()) {
            return null;
        }

        try {
            String cleanKey = privateKeyPem
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "");

            String sanitizedKey = cleanKey
                    .replace("\\n", "")
                    .replace("\\r", "")
                    .replace("\"", "")
                    .replace("'", "")
                    .replaceAll("\\s+", "")
                    .replace("\\", "");

            byte[] keyBytes = Base64.getDecoder().decode(sanitizedKey);

            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);

            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePrivate(keySpec);

        } catch (Exception e) {
            log.error("Error parsing private key", e);
            return null;
        }
    }
}
