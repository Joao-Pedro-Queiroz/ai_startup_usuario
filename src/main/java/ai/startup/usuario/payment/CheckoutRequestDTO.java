package ai.startup.usuario.payment;

public record CheckoutRequestDTO(
    String productId,  // "wins-pack" ou "brainwin-learn"
    String successUrl,
    String cancelUrl
) {}

