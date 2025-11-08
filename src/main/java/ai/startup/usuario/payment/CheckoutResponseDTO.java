package ai.startup.usuario.payment;

public record CheckoutResponseDTO(
    String sessionId,
    String checkoutUrl
) {}


