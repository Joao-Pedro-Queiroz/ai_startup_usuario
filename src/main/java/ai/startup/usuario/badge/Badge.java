package ai.startup.usuario.badge;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document("user_badges")
public class Badge {
    @Id
    private String id;
    private String userId;          // ID do usuário
    private String badgeId;         // ID do badge (ex: "perf_50", "streak_week")
    private String category;        // "desempenho" | "streak" | "especial"
    private String title;           // "50 Questões"
    private String rarity;          // "comum" | "raro" | "épico" | "lendario"
    private String img;             // URL/path da imagem
    private Integer current;        // Progresso atual
    private Integer target;         // Meta
    private LocalDateTime earnedAt; // Quando foi conquistado
    private Boolean claimed;        // Se recompensa foi resgatada
    private Integer xpReward;       // XP ganho
    private Integer coinsReward;    // Coins ganhos
}

