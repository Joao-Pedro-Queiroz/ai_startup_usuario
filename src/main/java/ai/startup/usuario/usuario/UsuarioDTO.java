package ai.startup.usuario.usuario;

import java.time.LocalDate;
import java.util.List;

public record UsuarioDTO(
        String id,
        String nome,
        String sobrenome,
        String cpf,
        String telefone,
        LocalDate nascimento,
        String email,
        String language,
        Long wins,
        Long streaks,
        Long xp,
        String permissao,
        Boolean isPremium,
        Boolean extendedTime,
        String selectedPractice,
        List<String> selectedPractices
) {}
