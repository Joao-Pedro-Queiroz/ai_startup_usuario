package ai.startup.usuario.payment;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * Registro de pagamentos processados para evitar duplicação
 */
@Document(collection = "processed_payments")
public class ProcessedPayment {
    
    @Id
    private String sessionId;
    private String userId;
    private String productId;
    private LocalDateTime processedAt;
    
    public ProcessedPayment() {}
    
    public ProcessedPayment(String sessionId, String userId, String productId) {
        this.sessionId = sessionId;
        this.userId = userId;
        this.productId = productId;
        this.processedAt = LocalDateTime.now();
    }
    
    // Getters e Setters
    public String getSessionId() {
        return sessionId;
    }
    
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public String getProductId() {
        return productId;
    }
    
    public void setProductId(String productId) {
        this.productId = productId;
    }
    
    public LocalDateTime getProcessedAt() {
        return processedAt;
    }
    
    public void setProcessedAt(LocalDateTime processedAt) {
        this.processedAt = processedAt;
    }
}



