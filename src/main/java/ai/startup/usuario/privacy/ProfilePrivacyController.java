package ai.startup.usuario.privacy;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users/{userId}/privacy")
public class ProfilePrivacyController {
    
    @Autowired
    private ProfilePrivacyService service;
    
    /**
     * GET /users/{userId}/privacy - Busca configurações de privacidade
     */
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping
    public ResponseEntity<ProfilePrivacyDTO> getPrivacySettings(@PathVariable String userId) {
        return ResponseEntity.ok(service.getPrivacySettings(userId));
    }
    
    /**
     * PUT /users/{userId}/privacy - Atualiza configurações de privacidade
     */
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping
    public ResponseEntity<ProfilePrivacyDTO> updatePrivacySettings(
        @PathVariable String userId,
        @RequestBody ProfilePrivacyDTO dto
    ) {
        return ResponseEntity.ok(service.updatePrivacySettings(userId, dto));
    }
    
    /**
     * DELETE /users/{userId}/privacy - Deleta configurações (volta ao padrão)
     */
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping
    public ResponseEntity<Void> deletePrivacySettings(@PathVariable String userId) {
        service.deletePrivacySettings(userId);
        return ResponseEntity.noContent().build();
    }
}

