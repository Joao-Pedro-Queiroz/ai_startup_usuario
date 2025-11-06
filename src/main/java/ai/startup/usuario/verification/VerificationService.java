package ai.startup.usuario.verification;

import ai.startup.usuario.usuario.Usuario;
import ai.startup.usuario.usuario.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class VerificationService {
    
    @Autowired
    private VerificationCodeRepository codeRepo;
    
    @Autowired
    private UsuarioRepository usuarioRepo;
    
    private static final int EXPIRATION_MINUTES = 15;
    
    /**
     * Gera um código de 6 dígitos
     */
    private String generateCode() {
        SecureRandom random = new SecureRandom();
        int code = 100000 + random.nextInt(900000); // 100000 a 999999
        return String.valueOf(code);
    }
    
    /**
     * Envia código de verificação de email (para registro)
     */
    public void sendEmailVerificationCode(String email) {
        if (email == null || email.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email é obrigatório");
        }
        
        email = email.toLowerCase().trim();
        
        // Verifica se email já está cadastrado
        if (usuarioRepo.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email já cadastrado");
        }
        
        // Remove códigos antigos deste email
        codeRepo.deleteByEmail(email);
        
        // Cria novo código
        VerificationCode vc = new VerificationCode();
        vc.setEmail(email);
        vc.setCode(generateCode());
        vc.setType("EMAIL_VERIFICATION");
        vc.setCreatedAt(LocalDateTime.now());
        vc.setExpiresAt(LocalDateTime.now().plusMinutes(EXPIRATION_MINUTES));
        vc.setUsed(false);
        
        codeRepo.save(vc);
        
        // TODO: Integrar com serviço de email (SendGrid, AWS SES, etc)
        // Por enquanto, vamos apenas logar o código no console para desenvolvimento
        System.out.println("==================================");
        System.out.println("📧 Código de verificação para: " + email);
        System.out.println("🔐 Código: " + vc.getCode());
        System.out.println("⏰ Expira em: " + EXPIRATION_MINUTES + " minutos");
        System.out.println("==================================");
    }
    
    /**
     * Verifica o código de email
     */
    public boolean verifyEmailCode(String email, String code) {
        if (email == null || code == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email e código são obrigatórios");
        }
        
        email = email.toLowerCase().trim();
        
        Optional<VerificationCode> vcOpt = codeRepo.findByEmailAndCodeAndType(email, code, "EMAIL_VERIFICATION");
        
        if (vcOpt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Código inválido");
        }
        
        VerificationCode vc = vcOpt.get();
        
        if (vc.getUsed()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Código já foi utilizado");
        }
        
        if (LocalDateTime.now().isAfter(vc.getExpiresAt())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Código expirado");
        }
        
        // Marca como usado
        vc.setUsed(true);
        codeRepo.save(vc);
        
        return true;
    }
    
    /**
     * Envia código de recuperação de senha
     */
    public void sendPasswordResetCode(String email) {
        if (email == null || email.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email é obrigatório");
        }
        
        email = email.toLowerCase().trim();
        
        // Verifica se usuário existe
        Usuario user = usuarioRepo.findByEmail(email)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));
        
        // Remove códigos antigos deste email
        codeRepo.deleteByEmail(email);
        
        // Cria novo código
        VerificationCode vc = new VerificationCode();
        vc.setEmail(email);
        vc.setCode(generateCode());
        vc.setType("PASSWORD_RESET");
        vc.setCreatedAt(LocalDateTime.now());
        vc.setExpiresAt(LocalDateTime.now().plusMinutes(EXPIRATION_MINUTES));
        vc.setUsed(false);
        vc.setUserId(user.getId());
        
        codeRepo.save(vc);
        
        // TODO: Integrar com serviço de email
        System.out.println("==================================");
        System.out.println("🔐 Código de recuperação de senha para: " + email);
        System.out.println("🔑 Código: " + vc.getCode());
        System.out.println("⏰ Expira em: " + EXPIRATION_MINUTES + " minutos");
        System.out.println("==================================");
    }
    
    /**
     * Verifica código de reset e retorna o userId
     */
    public String verifyPasswordResetCode(String email, String code) {
        if (email == null || code == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email e código são obrigatórios");
        }
        
        email = email.toLowerCase().trim();
        
        Optional<VerificationCode> vcOpt = codeRepo.findByEmailAndCodeAndType(email, code, "PASSWORD_RESET");
        
        if (vcOpt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Código inválido");
        }
        
        VerificationCode vc = vcOpt.get();
        
        if (vc.getUsed()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Código já foi utilizado");
        }
        
        if (LocalDateTime.now().isAfter(vc.getExpiresAt())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Código expirado");
        }
        
        // Marca como usado
        vc.setUsed(true);
        codeRepo.save(vc);
        
        return vc.getUserId();
    }
}

