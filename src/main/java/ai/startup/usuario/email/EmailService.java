package ai.startup.usuario.email;

import com.sendgrid.*;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class EmailService {
    
    @Value("${sendgrid.api.key}")
    private String sendGridApiKey;
    
    @Value("${sendgrid.from.email}")
    private String fromEmail;
    
    @Value("${sendgrid.from.name}")
    private String fromName;
    
    /**
     * Envia código de verificação de email para novo cadastro
     */
    public void sendVerificationCode(String toEmail, String code) throws IOException {
        Email from = new Email(fromEmail, fromName);
        Email to = new Email(toEmail);
        String subject = "Seu código de verificação - BrainWin";
        
        String htmlContent = """
            <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px;">
                <h2 style="color: #ff1b8d;">🎓 BrainWin</h2>
                <p>Olá!</p>
                <p>Seu código de verificação é:</p>
                <div style="background: #f0f0f0; padding: 20px; text-align: center; border-radius: 8px; margin: 20px 0;">
                    <h1 style="color: #14f195; letter-spacing: 8px; margin: 0; font-size: 32px;">%s</h1>
                </div>
                <p>Este código expira em <strong>15 minutos</strong>.</p>
                <p>Se você não solicitou este código, ignore este email.</p>
                <hr style="margin: 30px 0; border: none; border-top: 1px solid #e0e0e0;">
                <p style="color: #999; font-size: 12px;">
                    BrainWin - Treine para o SAT com inteligência artificial
                </p>
            </div>
        """.formatted(code);
        
        Content content = new Content("text/html", htmlContent);
        Mail mail = new Mail(from, subject, to, content);
        
        SendGrid sg = new SendGrid(sendGridApiKey);
        Request request = new Request();
        
        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            Response response = sg.api(request);
            
            if (response.getStatusCode() >= 400) {
                throw new IOException("Erro ao enviar email: " + response.getStatusCode() + " - " + response.getBody());
            }
        } catch (IOException ex) {
            throw new IOException("Falha ao enviar email via SendGrid: " + ex.getMessage(), ex);
        }
    }
    
    /**
     * Envia código de recuperação de senha
     */
    public void sendPasswordResetCode(String toEmail, String code) throws IOException {
        Email from = new Email(fromEmail, fromName);
        Email to = new Email(toEmail);
        String subject = "Recuperação de senha - BrainWin";
        
        String htmlContent = """
            <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px;">
                <h2 style="color: #ff1b8d;">🔐 BrainWin</h2>
                <p>Olá!</p>
                <p>Você solicitou a recuperação de senha.</p>
                <p>Seu código de recuperação é:</p>
                <div style="background: #f0f0f0; padding: 20px; text-align: center; border-radius: 8px; margin: 20px 0;">
                    <h1 style="color: #14f195; letter-spacing: 8px; margin: 0; font-size: 32px;">%s</h1>
                </div>
                <p>Este código expira em <strong>15 minutos</strong>.</p>
                <p><strong>Se você não solicitou esta recuperação, ignore este email e sua senha permanecerá segura.</strong></p>
                <hr style="margin: 30px 0; border: none; border-top: 1px solid #e0e0e0;">
                <p style="color: #999; font-size: 12px;">
                    BrainWin - Treine para o SAT com inteligência artificial
                </p>
            </div>
        """.formatted(code);
        
        Content content = new Content("text/html", htmlContent);
        Mail mail = new Mail(from, subject, to, content);
        
        SendGrid sg = new SendGrid(sendGridApiKey);
        Request request = new Request();
        
        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            Response response = sg.api(request);
            
            if (response.getStatusCode() >= 400) {
                throw new IOException("Erro ao enviar email: " + response.getStatusCode() + " - " + response.getBody());
            }
        } catch (IOException ex) {
            throw new IOException("Falha ao enviar email via SendGrid: " + ex.getMessage(), ex);
        }
    }
}

