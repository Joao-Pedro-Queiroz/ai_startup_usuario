package ai.startup.usuario.plano;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Map;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
@Document("UserPlans")
public class UserPlan {
    @Id
    private String id;

    private String user_id;                // igual ao template
    private Map<String,Object> topics;     // copia do template
}
