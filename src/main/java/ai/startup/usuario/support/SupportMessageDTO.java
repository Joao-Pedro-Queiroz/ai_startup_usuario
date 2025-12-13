package ai.startup.usuario.support;

import java.time.LocalDateTime;

public record SupportMessageDTO(
    String id,
    String userId,
    String email,
    String nome,
    String message,
    LocalDateTime data,
    String status
) {}

