package ai.startup.usuario.support;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface SupportMessageRepository extends MongoRepository<SupportMessage, String> {
    List<SupportMessage> findByUserId(String userId);
    List<SupportMessage> findByStatus(String status);
    List<SupportMessage> findByEmail(String email);
}

