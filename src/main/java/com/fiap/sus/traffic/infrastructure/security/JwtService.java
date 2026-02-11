package com.fiap.sus.traffic.infrastructure.security;

import com.fiap.sus.traffic.infrastructure.config.TrafficIntelligenceProperties;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.Date;

/**
 * Serviço para gerar tokens JWT para autenticação service-to-service.
 * Usa a chave privada do Traffic Intelligence Service para assinar tokens.
 */
@Service
@Slf4j
public class JwtService {

    private final PrivateKey privateKey;
    private static final String ISSUER = "SusConnect-TrafficIntelligence";
    private static final long TOKEN_VALIDITY_MS = 3600000; // 1 hora

    public JwtService(TrafficIntelligenceProperties properties) {
        String privateKeyPem = properties.getLiveopsService().getPrivateKey();
        
        // Log detalhado para diagnóstico
        if (privateKeyPem == null) {
            log.error("❌ TRAFFIC_LIVEOPS_PRIVATE_KEY é null! Verifique se o secret está configurado no Cloud Run e se a propriedade está sendo lida corretamente.");
            log.error("Properties object: {}", properties != null ? "não-nulo" : "nulo");
            if (properties != null && properties.getLiveopsService() != null) {
                log.error("LiveOpsService properties: {}", properties.getLiveopsService());
            }
            this.privateKey = null;
        } else if (privateKeyPem.isBlank()) {
            log.error("❌ TRAFFIC_LIVEOPS_PRIVATE_KEY está vazia (blank)! Verifique se o secret tem conteúdo.");
            this.privateKey = null;
        } else {
            log.info("✅ Chave privada encontrada (tamanho: {} caracteres). Fazendo parse...", privateKeyPem.length());
            log.debug("Primeiros 50 caracteres da chave: {}", privateKeyPem.substring(0, Math.min(50, privateKeyPem.length())));
            this.privateKey = parsePrivateKey(privateKeyPem);
            if (this.privateKey == null) {
                log.error("❌ Falha ao fazer parse da chave privada. Verifique o formato da chave.");
            } else {
                log.info("✅ Chave privada parseada com sucesso!");
            }
        }
    }

    /**
     * Gera um token JWT para autenticação com o LiveOps Service.
     * 
     * @return Token JWT como string
     */
    public String generateToken() {
        if (privateKey == null) {
            log.error("❌ Chave privada não configurada. Não é possível gerar token JWT. Verifique se TRAFFIC_LIVEOPS_PRIVATE_KEY está configurada no Cloud Run.");
            return null;
        }

        try {
            Date now = new Date();
            Date expiration = new Date(now.getTime() + TOKEN_VALIDITY_MS);

            return Jwts.builder()
                    .issuer(ISSUER)
                    .subject("traffic-intelligence-service")
                    .issuedAt(now)
                    .expiration(expiration)
                    .claim("service", "traffic-intelligence")
                    .signWith(privateKey)
                    .compact();
        } catch (Exception e) {
            log.error("Erro ao gerar token JWT", e);
            return null;
        }
    }

    /**
     * Converte a chave privada PEM em objeto PrivateKey.
     * 
     * @param privateKeyPem Chave privada em formato PEM
     * @return PrivateKey ou null se não conseguir fazer o parse
     */
    private PrivateKey parsePrivateKey(String privateKeyPem) {
        if (privateKeyPem == null || privateKeyPem.isBlank()) {
            log.warn("Chave privada não fornecida. Autenticação com LiveOps Service não funcionará.");
            return null;
        }

        try {
            // Remove headers e footers PEM
            String privateKeyContent = privateKeyPem
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s", "");

            // Decodifica base64
            byte[] keyBytes = Base64.getDecoder().decode(privateKeyContent);

            // Cria a especificação da chave
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);

            // Gera a chave privada
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PrivateKey key = keyFactory.generatePrivate(keySpec);

            log.info("Chave privada carregada com sucesso para autenticação com LiveOps Service");
            return key;
        } catch (Exception e) {
            log.error("Erro ao fazer parse da chave privada", e);
            return null;
        }
    }
}
