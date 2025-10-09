package ai.startup.usuario.plano;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface UserPlanRepository extends MongoRepository<UserPlan, String> {
    Optional<UserPlan> findByUserId(String user_id);
}
