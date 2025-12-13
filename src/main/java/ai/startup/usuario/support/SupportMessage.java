package ai.startup.usuario.support;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document("support_messages")
public class SupportMessage {
    @Id
    private String id;
    
    private String userId;
    private String email;
    private String nome;
    private String message;
    private LocalDateTime data;
    private String status; // "NOVO", "EM_ATENDIMENTO", "RESOLVIDO"
}

