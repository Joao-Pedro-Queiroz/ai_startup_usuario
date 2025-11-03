package ai.startup.usuario.streak;

import ai.startup.usuario.usuario.Usuario;
import ai.startup.usuario.usuario.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Service
public class StreakService {
    
    @Autowired
    private UsuarioRepository usuarioRepo;
    
    /**
     * Atualiza o streak do usuário baseado no login
     * Retorna o streak atualizado
     */
    public Long updateStreakOnLogin(String userId) {
        Usuario user = usuarioRepo.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));
        
        LocalDate today = LocalDate.now();
        LocalDate lastLogin = user.getUltimoLogin();
        Long currentStreak = user.getStreaks() != null ? user.getStreaks() : 0L;
        
        if (lastLogin == null) {
            // Primeiro login
            user.setStreaks(1L);
            user.setUltimoLogin(today);
        } else {
            long daysBetween = ChronoUnit.DAYS.between(lastLogin, today);
            
            if (daysBetween == 0) {
                // Mesmo dia - mantém streak
                // Não faz nada
            } else if (daysBetween == 1) {
                // Login consecutivo - incrementa streak
                user.setStreaks(currentStreak + 1);
                user.setUltimoLogin(today);
            } else {
                // Quebrou o streak - reseta para 1
                user.setStreaks(1L);
                user.setUltimoLogin(today);
            }
        }
        
        Usuario saved = usuarioRepo.save(user);
        return saved.getStreaks();
    }
    
    /**
     * Calcula e retorna o streak atual sem atualizar
     */
    public Long getCurrentStreak(String userId) {
        Usuario user = usuarioRepo.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));
        
        return user.getStreaks() != null ? user.getStreaks() : 0L;
    }
}

