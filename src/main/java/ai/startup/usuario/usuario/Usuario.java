package ai.startup.usuario.usuario;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;

@Data                       // gera getters, setters, equals, hashCode, toString
@NoArgsConstructor          // construtor vazio
@AllArgsConstructor         // construtor com todos os args
@Document("usuarios")
public class Usuario {
    @Id
    private String id;
    private String nome;
    private String sobrenome;
    private String cpf;
    private String telefone;
    private LocalDate nascimento;
    private String email;
    private String senhaHash;
    private Long wins;
    private Long streaks;
    private Long xp;
    private String permissao;   // USER | ADMIN
}