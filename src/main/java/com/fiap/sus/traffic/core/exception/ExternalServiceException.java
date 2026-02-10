package com.fiap.sus.traffic.core.exception;

/**
 * Exceção lançada quando há problemas na comunicação com serviços externos.
 */
public class ExternalServiceException extends BusinessException {
    
    private final String serviceName;
    private final int statusCode;
    
    public ExternalServiceException(String serviceName, String message) {
        super("EXTERNAL_SERVICE_ERROR", 
            String.format("Erro ao comunicar com %s: %s", serviceName, message));
        this.serviceName = serviceName;
        this.statusCode = 0;
    }
    
    public ExternalServiceException(String serviceName, String message, int statusCode) {
        super("EXTERNAL_SERVICE_ERROR", 
            String.format("Erro ao comunicar com %s (Status %d): %s", serviceName, statusCode, message));
        this.serviceName = serviceName;
        this.statusCode = statusCode;
    }
    
    public ExternalServiceException(String serviceName, String message, Throwable cause) {
        super("EXTERNAL_SERVICE_ERROR", 
            String.format("Erro ao comunicar com %s: %s", serviceName, message), cause);
        this.serviceName = serviceName;
        this.statusCode = 0;
    }
    
    public String getServiceName() {
        return serviceName;
    }
    
    public int getStatusCode() {
        return statusCode;
    }
}
