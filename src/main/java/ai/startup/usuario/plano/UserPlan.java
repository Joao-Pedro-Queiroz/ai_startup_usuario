package ai.startup.usuario.plano;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Map;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
@Document("UserPlans")
public class UserPlan {
    @Id
    private String id;

    @Field("user_id")     // mapeia no Mongo
    private String userId;                // igual ao template
    private Map<String,Object> topics;     // copia do template
}
