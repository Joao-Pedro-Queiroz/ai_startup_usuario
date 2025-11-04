package ai.startup.usuario.usuario;

import ai.startup.usuario.auth.AuthRequestDTO;
import ai.startup.usuario.auth.AuthResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import java.util.List;

@RestController
@RequestMapping
public class UsuarioController {

    private final UsuarioService service;

    public UsuarioController(UsuarioService service) {
        this.service = service;
    }

    // AUTH
    @Operation(security = {})  
    @PostMapping("/auth/login")
    public ResponseEntity<AuthResponseDTO> login(@RequestBody AuthRequestDTO req) {
        return ResponseEntity.ok(service.autenticar(req));
    }

    @Operation(security = {}) // público na UI do Swagger
    @PostMapping("/auth/register")
    public ResponseEntity<AuthResponseDTO> register(@RequestBody UsuarioCreateDTO dto) {
        return ResponseEntity.ok(service.registrar(dto));
    }

    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/users/me")
    public ResponseEntity<UsuarioDTO> me(HttpServletRequest req) {
        String email = (String) req.getAttribute("authEmail"); // vindo do SecurityFilter
        if (email == null || email.isBlank()) {
            return ResponseEntity.status(401).build();
        }
        
        // Atualiza streak ao buscar /users/me
        UsuarioDTO user = service.obterPorEmail(email);
        if (user != null && user.id() != null) {
            service.updateStreakOnLogin(user.id());
            // Recarrega para pegar o streak atualizado
            user = service.obterPorEmail(email);
        }
        
        return ResponseEntity.ok(user);
    }

    // CRUD
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/users")
    public ResponseEntity<UsuarioDTO> criar(@RequestBody UsuarioCreateDTO dto) {
        return ResponseEntity.ok(service.criar(dto));
    }

    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/users")
    public ResponseEntity<List<UsuarioDTO>> listar() {
        return ResponseEntity.ok(service.listar());
    }

    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/users/{id}")
    public ResponseEntity<UsuarioDTO> obter(@PathVariable String id) {
        return ResponseEntity.ok(service.obter(id));
    }

    // update parcial: envie só os campos que quer mudar
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/users/{id}")
    public ResponseEntity<UsuarioDTO> atualizar(@PathVariable String id,
                                                @RequestBody UsuarioUpdateDTO dto,
                                                HttpServletRequest req) {
        String authPermissao = (String) req.getAttribute("authPermissao"); // setado no SecurityFilter
        return ResponseEntity.ok(service.atualizar(id, dto, authPermissao));
    }

    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deletar(@PathVariable String id) {
        service.deletar(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * POST /users/me/upgrade-premium - Faz upgrade para premium (custa 100 wins)
     */
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/users/me/upgrade-premium")
    public ResponseEntity<UsuarioDTO> upgradePremium(HttpServletRequest req) {
        String email = (String) req.getAttribute("authEmail");
        if (email == null || email.isBlank()) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(service.upgradeToPremium(email));
    }

    // ===== PERFIL PÚBLICO E RANKING =====
    
    /**
     * GET /users/{id}/public - Busca perfil público (filtrando dados privados)
     */
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/users/{id}/public")
    public ResponseEntity<PublicProfileDTO> getPublicProfile(@PathVariable String id) {
        return ResponseEntity.ok(service.getPublicProfile(id));
    }

    /**
     * GET /ranking/xp - Ranking de usuários por XP
     */
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/ranking/xp")
    public ResponseEntity<List<UsuarioDTO>> getRankingByXp(
        @RequestParam(defaultValue = "50") int limit
    ) {
        return ResponseEntity.ok(service.getRankingByXp(limit));
    }

    /**
     * GET /ranking/streak - Ranking de usuários por Streak
     */
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/ranking/streak")
    public ResponseEntity<List<UsuarioDTO>> getRankingByStreak(
        @RequestParam(defaultValue = "50") int limit
    ) {
        return ResponseEntity.ok(service.getRankingByStreak(limit));
    }
}
