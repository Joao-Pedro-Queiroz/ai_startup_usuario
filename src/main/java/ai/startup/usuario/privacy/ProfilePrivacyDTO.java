package ai.startup.usuario.privacy;

public record ProfilePrivacyDTO(
    String id,
    String userId,
    Boolean winsPublic,
    Boolean streaksPublic,
    Boolean xpPublic,
    Boolean bestScorePublic,
    Boolean simuladosCountPublic,
    Boolean badgesPublic,
    Boolean emailPublic,
    Boolean telefonePublic
) {}

