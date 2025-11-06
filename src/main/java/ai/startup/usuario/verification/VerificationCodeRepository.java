package ai.startup.usuario.verification;

import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;
import java.util.List;

public interface VerificationCodeRepository extends MongoRepository<VerificationCode, String> {
    Optional<VerificationCode> findByEmailAndCodeAndType(String email, String code, String type);
    List<VerificationCode> findByEmailAndType(String email, String type);
    void deleteByEmail(String email);
}

