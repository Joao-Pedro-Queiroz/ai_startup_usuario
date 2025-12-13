package ai.startup.usuario.payment;

import java.time.LocalDateTime;

/**
 * DTO para retornar histórico de compras do usuário
 */
public record PaymentHistoryDTO(
    String sessionId,
    String productId,
    String productName,
    Long priceInCents,
    String currency,
    Integer wins,
    Boolean isSubscription,
    LocalDateTime processedAt
) {}

