package ai.startup.usuario.usuario;

import java.time.LocalDate;

public record UsuarioCreateDTO(
        String nome,
        String sobrenome,
        String cpf,
        String telefone,
        LocalDate nascimento,
        String email,
        String senha,
        String permissao,
        String language
) {}
