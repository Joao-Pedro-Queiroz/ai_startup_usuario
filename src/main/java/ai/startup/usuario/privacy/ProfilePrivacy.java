package ai.startup.usuario.privacy;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document("profile_privacy")
public class ProfilePrivacy {
    @Id
    private String id;
    private String userId;
    
    // Configurações de privacidade (true = público, false = privado)
    private Boolean winsPublic;
    private Boolean streaksPublic;
    private Boolean xpPublic;
    private Boolean bestScorePublic;
    private Boolean simuladosCountPublic;
    private Boolean badgesPublic;
    private Boolean emailPublic;
    private Boolean telefonePublic;
    
    // Método helper para criar configuração padrão (tudo privado exceto badges e streak)
    public static ProfilePrivacy createDefault(String userId) {
        ProfilePrivacy privacy = new ProfilePrivacy();
        privacy.setUserId(userId);
        privacy.setWinsPublic(false);
        privacy.setStreaksPublic(true);  // Streak público por padrão (para ranking)
        privacy.setXpPublic(true);       // XP público por padrão (para ranking)
        privacy.setBestScorePublic(false);
        privacy.setSimuladosCountPublic(false);
        privacy.setBadgesPublic(true);   // Badges públicos por padrão
        privacy.setEmailPublic(false);
        privacy.setTelefonePublic(false);
        return privacy;
    }
}

