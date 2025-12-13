package ai.startup.usuario.support;

import ai.startup.usuario.usuario.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/support")
public class SupportController {
    
    @Autowired
    private SupportService supportService;
    
    @Autowired
    private UsuarioService usuarioService;
    
    /**
     * POST /support/messages - Cria uma nova mensagem de suporte (requer autenticação)
     * Body: { message }
     * Os dados do usuário (userId, email, nome) são obtidos do token JWT
     */
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Envia mensagem de suporte")
    @PostMapping("/messages")
    public ResponseEntity<SupportMessageDTO> criarMensagem(
            @RequestBody Map<String, String> request,
            HttpServletRequest httpRequest) {
        
        // Obtém email do token JWT (setado pelo SecurityFilter)
        String email = (String) httpRequest.getAttribute("authEmail");
        if (email == null || email.isBlank()) {
            return ResponseEntity.status(401).build();
        }
        
        // Busca dados do usuário pelo email
        var usuarioDTO = usuarioService.obterPorEmail(email);
        if (usuarioDTO == null) {
            return ResponseEntity.status(404).build();
        }
        
        // Cria DTO com dados do usuário autenticado
        SupportMessageCreateDTO dto = new SupportMessageCreateDTO(
                usuarioDTO.id(),
                usuarioDTO.email(),
                usuarioDTO.nome() + (usuarioDTO.sobrenome() != null ? " " + usuarioDTO.sobrenome() : ""),
                request.get("message")
        );
        
        SupportMessageDTO saved = supportService.criarMensagem(dto);
        return ResponseEntity.ok(saved);
    }
}

