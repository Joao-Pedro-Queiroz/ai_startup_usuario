package ai.startup.usuario.payment;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface ProcessedPaymentRepository extends MongoRepository<ProcessedPayment, String> {
    boolean existsBySessionId(String sessionId);
}



