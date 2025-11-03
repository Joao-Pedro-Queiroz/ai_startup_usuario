package ai.startup.usuario.badge;

import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;
import java.util.Optional;

public interface BadgeRepository extends MongoRepository<Badge, String> {
    List<Badge> findByUserId(String userId);
    List<Badge> findByUserIdAndCategory(String userId, String category);
    List<Badge> findByUserIdAndEarnedAtIsNotNull(String userId); // Badges conquistados
    Optional<Badge> findByUserIdAndBadgeId(String userId, String badgeId);
    void deleteByUserId(String userId);
}

