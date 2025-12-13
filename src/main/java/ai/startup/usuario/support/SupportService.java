package ai.startup.usuario.support;

import ai.startup.usuario.email.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;

@Service
public class SupportService {
    
    @Autowired
    private SupportMessageRepository repository;
    
    @Autowired
    private EmailService emailService;
    
    /**
     * Cria uma nova mensagem de suporte, salva no banco e envia notificação por email
     */
    public SupportMessageDTO criarMensagem(SupportMessageCreateDTO dto) {
        // Cria e salva a mensagem no banco
        SupportMessage message = SupportMessage.builder()
                .userId(dto.userId())
                .email(dto.email())
                .nome(dto.nome())
                .message(dto.message())
                .data(LocalDateTime.now())
                .status("NOVO")
                .build();
        
        SupportMessage saved = repository.save(message);
        
        // Envia email de notificação para a equipe de suporte
        try {
            emailService.sendSupportNotification(
                    dto.nome(),
                    dto.email(),
                    dto.message()
            );
            System.out.println("✅ Email de notificação de suporte enviado com sucesso");
        } catch (IOException e) {
            System.err.println("❌ Erro ao enviar email de notificação de suporte: " + e.getMessage());
            // Não lança exceção - a mensagem já foi salva no banco
            // O email é secundário, o importante é que a mensagem foi registrada
        }
        
        return toDTO(saved);
    }
    
    private SupportMessageDTO toDTO(SupportMessage message) {
        return new SupportMessageDTO(
                message.getId(),
                message.getUserId(),
                message.getEmail(),
                message.getNome(),
                message.getMessage(),
                message.getData(),
                message.getStatus()
        );
    }
}

