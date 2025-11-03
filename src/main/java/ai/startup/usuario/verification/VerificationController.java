package ai.startup.usuario.verification;

import ai.startup.usuario.auth.AuthResponseDTO;
import ai.startup.usuario.usuario.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class VerificationController {
    
    @Autowired
    private VerificationService verificationService;
    
    @Autowired
    private UsuarioService usuarioService;
    
    /**
     * Envia código de verificação de email
     */
    @Operation(security = {})
    @PostMapping("/send-verification-code")
    public ResponseEntity<Map<String, String>> sendVerificationCode(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        verificationService.sendEmailVerificationCode(email);
        return ResponseEntity.ok(Map.of(
            "message", "Código enviado com sucesso",
            "email", email
        ));
    }
    
    /**
     * Verifica código de email
     */
    @Operation(security = {})
    @PostMapping("/verify-email-code")
    public ResponseEntity<Map<String, Boolean>> verifyEmailCode(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String code = request.get("code");
        boolean valid = verificationService.verifyEmailCode(email, code);
        return ResponseEntity.ok(Map.of("valid", valid));
    }
    
    /**
     * Envia código de recuperação de senha
     */
    @Operation(security = {})
    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        verificationService.sendPasswordResetCode(email);
        return ResponseEntity.ok(Map.of(
            "message", "Código de recuperação enviado",
            "email", email
        ));
    }
    
    /**
     * Reseta senha usando código
     */
    @Operation(security = {})
    @PostMapping("/reset-password")
    public ResponseEntity<AuthResponseDTO> resetPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String code = request.get("code");
        String newPassword = request.get("newPassword");
        
        // Verifica código e obtém userId
        String userId = verificationService.verifyPasswordResetCode(email, code);
        
        // Atualiza senha
        usuarioService.resetPassword(userId, newPassword);
        
        // Faz login automático após reset
        return ResponseEntity.ok(usuarioService.autenticar(
            new ai.startup.usuario.auth.AuthRequestDTO(email, newPassword)
        ));
    }
}

