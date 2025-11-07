package ai.startup.usuario.usuario;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.util.List;

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
    private LocalDate ultimoLogin; // Data do último login para cálculo de streak
    private Boolean isPremium;  // true se usuário tem acesso premium
    private Boolean extendedTime; // true se usuário tem direito a tempo estendido (53 min ao invés de 35 min)
    private String selectedPractice; // prática selecionada pelo usuário (ex: "SAT_MATH", null se ainda não selecionou) - DEPRECATED: use selectedPractices
    private List<String> selectedPractices; // lista de práticas ativas do usuário (ex: ["sat-math", "sat-reading"])
}