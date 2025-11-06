package ai.startup.usuario.badge;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class BadgeService {
    
    @Autowired
    private BadgeRepository repo;
    
    /**
     * Busca todos os badges de um usuário
     */
    public List<BadgeDTO> getUserBadges(String userId) {
        return repo.findByUserId(userId).stream()
            .map(this::toDTO)
            .toList();
    }
    
    /**
     * Busca badges por categoria
     */
    public List<BadgeDTO> getUserBadgesByCategory(String userId, String category) {
        return repo.findByUserIdAndCategory(userId, category).stream()
            .map(this::toDTO)
            .toList();
    }
    
    /**
     * Busca apenas badges conquistados
     */
    public List<BadgeDTO> getEarnedBadges(String userId) {
        return repo.findByUserIdAndEarnedAtIsNotNull(userId).stream()
            .map(this::toDTO)
            .toList();
    }
    
    /**
     * Atualiza progresso de um badge
     */
    public BadgeDTO updateProgress(String userId, String badgeId, Integer current) {
        Optional<Badge> existing = repo.findByUserIdAndBadgeId(userId, badgeId);
        
        Badge badge;
        if (existing.isPresent()) {
            badge = existing.get();
            badge.setCurrent(current);
            
            // Se atingiu o target e ainda não foi conquistado, marcar como conquistado
            if (current >= badge.getTarget() && badge.getEarnedAt() == null) {
                badge.setEarnedAt(LocalDateTime.now());
            }
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Badge não encontrado");
        }
        
        return toDTO(repo.save(badge));
    }
    
    /**
     * Concede um badge ao usuário
     */
    public BadgeDTO earnBadge(String userId, String badgeId, String category, String title, 
                              String rarity, String img, Integer xp, Integer coins) {
        Optional<Badge> existing = repo.findByUserIdAndBadgeId(userId, badgeId);
        
        if (existing.isPresent() && existing.get().getEarnedAt() != null) {
            // Badge já foi conquistado
            return toDTO(existing.get());
        }
        
        Badge badge;
        if (existing.isPresent()) {
            badge = existing.get();
            badge.setEarnedAt(LocalDateTime.now());
        } else {
            badge = new Badge();
            badge.setUserId(userId);
            badge.setBadgeId(badgeId);
            badge.setCategory(category);
            badge.setTitle(title);
            badge.setRarity(rarity);
            badge.setImg(img);
            badge.setCurrent(1);
            badge.setTarget(1);
            badge.setEarnedAt(LocalDateTime.now());
            badge.setClaimed(false);
            badge.setXpReward(xp);
            badge.setCoinsReward(coins);
        }
        
        return toDTO(repo.save(badge));
    }
    
    /**
     * Cria ou atualiza um badge com progresso
     */
    public BadgeDTO upsertBadge(String userId, String badgeId, String category, String title,
                                String rarity, String img, Integer current, Integer target,
                                Integer xp, Integer coins) {
        Optional<Badge> existing = repo.findByUserIdAndBadgeId(userId, badgeId);
        
        Badge badge;
        if (existing.isPresent()) {
            badge = existing.get();
            badge.setCurrent(current);
            
            // Se atingiu o target e ainda não foi conquistado, marcar como conquistado
            if (current >= target && badge.getEarnedAt() == null) {
                badge.setEarnedAt(LocalDateTime.now());
            }
        } else {
            badge = new Badge();
            badge.setUserId(userId);
            badge.setBadgeId(badgeId);
            badge.setCategory(category);
            badge.setTitle(title);
            badge.setRarity(rarity);
            badge.setImg(img);
            badge.setCurrent(current);
            badge.setTarget(target);
            badge.setClaimed(false);
            badge.setXpReward(xp);
            badge.setCoinsReward(coins);
            
            // Se já atingiu o target no upsert, marcar como conquistado
            if (current >= target) {
                badge.setEarnedAt(LocalDateTime.now());
            }
        }
        
        return toDTO(repo.save(badge));
    }
    
    /**
     * Deleta todos os badges de um usuário (útil para reset)
     */
    public void deleteUserBadges(String userId) {
        repo.deleteByUserId(userId);
    }
    
    private BadgeDTO toDTO(Badge b) {
        return new BadgeDTO(
            b.getId(),
            b.getUserId(),
            b.getBadgeId(),
            b.getCategory(),
            b.getTitle(),
            b.getRarity(),
            b.getImg(),
            b.getCurrent(),
            b.getTarget(),
            b.getEarnedAt(),
            b.getClaimed(),
            b.getXpReward(),
            b.getCoinsReward()
        );
    }
}

