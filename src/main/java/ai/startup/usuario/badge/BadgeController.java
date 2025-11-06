package ai.startup.usuario.badge;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users/{userId}/badges")
public class BadgeController {
    
    @Autowired
    private BadgeService service;
    
    /**
     * GET /users/{userId}/badges - Busca todos os badges do usuário
     */
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping
    public ResponseEntity<List<BadgeDTO>> getUserBadges(@PathVariable String userId) {
        return ResponseEntity.ok(service.getUserBadges(userId));
    }
    
    /**
     * GET /users/{userId}/badges/earned - Busca apenas badges conquistados
     */
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/earned")
    public ResponseEntity<List<BadgeDTO>> getEarnedBadges(@PathVariable String userId) {
        return ResponseEntity.ok(service.getEarnedBadges(userId));
    }
    
    /**
     * GET /users/{userId}/badges/category/{category} - Busca badges por categoria
     */
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/category/{category}")
    public ResponseEntity<List<BadgeDTO>> getBadgesByCategory(
        @PathVariable String userId, 
        @PathVariable String category
    ) {
        return ResponseEntity.ok(service.getUserBadgesByCategory(userId, category));
    }
    
    /**
     * POST /users/{userId}/badges - Cria ou atualiza um badge
     * Body: { badgeId, category, title, rarity, img, current, target, xp, coins }
     */
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping
    public ResponseEntity<BadgeDTO> upsertBadge(
        @PathVariable String userId,
        @RequestBody Map<String, Object> body
    ) {
        String badgeId = (String) body.get("badgeId");
        String category = (String) body.get("category");
        String title = (String) body.get("title");
        String rarity = (String) body.get("rarity");
        String img = (String) body.get("img");
        Integer current = (Integer) body.get("current");
        Integer target = (Integer) body.get("target");
        Integer xp = (Integer) body.getOrDefault("xp", 0);
        Integer coins = (Integer) body.getOrDefault("coins", 0);
        
        BadgeDTO result = service.upsertBadge(userId, badgeId, category, title, rarity, img, 
                                              current, target, xp, coins);
        return ResponseEntity.ok(result);
    }
    
    /**
     * PUT /users/{userId}/badges/{badgeId}/progress - Atualiza progresso
     * Body: { current }
     */
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/{badgeId}/progress")
    public ResponseEntity<BadgeDTO> updateProgress(
        @PathVariable String userId,
        @PathVariable String badgeId,
        @RequestBody Map<String, Integer> body
    ) {
        Integer current = body.get("current");
        return ResponseEntity.ok(service.updateProgress(userId, badgeId, current));
    }
    
    /**
     * DELETE /users/{userId}/badges - Deleta todos os badges (reset)
     */
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping
    public ResponseEntity<Void> deleteUserBadges(@PathVariable String userId) {
        service.deleteUserBadges(userId);
        return ResponseEntity.noContent().build();
    }
}

