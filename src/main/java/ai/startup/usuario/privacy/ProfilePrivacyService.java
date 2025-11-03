package ai.startup.usuario.privacy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ProfilePrivacyService {
    
    @Autowired
    private ProfilePrivacyRepository repo;
    
    /**
     * Busca configurações de privacidade do usuário (cria padrão se não existir)
     */
    public ProfilePrivacyDTO getPrivacySettings(String userId) {
        Optional<ProfilePrivacy> existing = repo.findByUserId(userId);
        
        if (existing.isEmpty()) {
            // Cria configuração padrão
            ProfilePrivacy defaultPrivacy = ProfilePrivacy.createDefault(userId);
            ProfilePrivacy saved = repo.save(defaultPrivacy);
            return toDTO(saved);
        }
        
        return toDTO(existing.get());
    }
    
    /**
     * Atualiza configurações de privacidade
     */
    public ProfilePrivacyDTO updatePrivacySettings(String userId, ProfilePrivacyDTO dto) {
        ProfilePrivacy privacy = repo.findByUserId(userId)
            .orElse(ProfilePrivacy.createDefault(userId));
        
        // Atualiza apenas os campos não-nulos do DTO
        if (dto.winsPublic() != null) privacy.setWinsPublic(dto.winsPublic());
        if (dto.streaksPublic() != null) privacy.setStreaksPublic(dto.streaksPublic());
        if (dto.xpPublic() != null) privacy.setXpPublic(dto.xpPublic());
        if (dto.bestScorePublic() != null) privacy.setBestScorePublic(dto.bestScorePublic());
        if (dto.simuladosCountPublic() != null) privacy.setSimuladosCountPublic(dto.simuladosCountPublic());
        if (dto.badgesPublic() != null) privacy.setBadgesPublic(dto.badgesPublic());
        if (dto.emailPublic() != null) privacy.setEmailPublic(dto.emailPublic());
        if (dto.telefonePublic() != null) privacy.setTelefonePublic(dto.telefonePublic());
        
        return toDTO(repo.save(privacy));
    }
    
    /**
     * Deleta configurações de privacidade
     */
    public void deletePrivacySettings(String userId) {
        repo.deleteByUserId(userId);
    }
    
    private ProfilePrivacyDTO toDTO(ProfilePrivacy p) {
        return new ProfilePrivacyDTO(
            p.getId(),
            p.getUserId(),
            p.getWinsPublic(),
            p.getStreaksPublic(),
            p.getXpPublic(),
            p.getBestScorePublic(),
            p.getSimuladosCountPublic(),
            p.getBadgesPublic(),
            p.getEmailPublic(),
            p.getTelefonePublic()
        );
    }
}

