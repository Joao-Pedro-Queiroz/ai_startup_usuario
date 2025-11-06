package ai.startup.usuario.privacy;

import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface ProfilePrivacyRepository extends MongoRepository<ProfilePrivacy, String> {
    Optional<ProfilePrivacy> findByUserId(String userId);
    void deleteByUserId(String userId);
}

