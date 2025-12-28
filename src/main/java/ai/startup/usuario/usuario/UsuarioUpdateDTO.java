package ai.startup.usuario.usuario;

import java.time.LocalDate;
import java.util.List;

public record UsuarioUpdateDTO(
        String nome,
        String sobrenome,
        String telefone,
        LocalDate nascimento,
        String email,
        String language,
        String cpf,
        String senha,
        Long wins,
        Long streaks,
        Long xp,
        String permissao,
        Boolean extendedTime,
        String selectedPractice,
        List<String> selectedPractices
) {}
