package ai.startup.usuario.usuario;

import java.util.List;

/**
 * DTO para perfil público - apenas informações que o usuário permitiu
 */
public record PublicProfileDTO(
    String id,
    String nome,
    String sobrenome,
    String email,        // Apenas se emailPublic = true
    String telefone,     // Apenas se telefonePublic = true
    Long wins,           // Apenas se winsPublic = true
    Long streaks,        // Apenas se streaksPublic = true
    Long xp,             // Apenas se xpPublic = true
    Integer bestScore,   // Apenas se bestScorePublic = true, calculado no frontend
    Integer simuladosCount, // Apenas se simuladosCountPublic = true, calculado no frontend
    List<String> badges  // Apenas se badgesPublic = true, IDs dos badges conquistados
) {}

