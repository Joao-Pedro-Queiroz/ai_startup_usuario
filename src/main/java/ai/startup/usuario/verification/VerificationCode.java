package ai.startup.usuario.verification;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document("verification_codes")
public class VerificationCode {
    @Id
    private String id;
    private String email;          // Email para qual o código foi enviado
    private String code;           // Código de 6 dígitos
    private String type;           // EMAIL_VERIFICATION | PASSWORD_RESET
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private Boolean used;          // true se já foi usado
    private String userId;         // ID do usuário (opcional, para password reset)
}

