package ai.startup.usuario.support;

public record SupportMessageCreateDTO(
    String userId,
    String email,
    String nome,
    String message
) {}

