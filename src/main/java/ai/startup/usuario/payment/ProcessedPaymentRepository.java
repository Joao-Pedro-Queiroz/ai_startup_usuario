package ai.startup.usuario.payment;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ProcessedPaymentRepository extends MongoRepository<ProcessedPayment, String> {
    boolean existsBySessionId(String sessionId);
    List<ProcessedPayment> findByUserIdOrderByProcessedAtDesc(String userId);
}



