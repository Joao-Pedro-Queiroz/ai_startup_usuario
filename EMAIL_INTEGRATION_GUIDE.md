# 📧 Guia de Integração de Email

## Como está agora (Desenvolvimento)
Os códigos são apenas **logados no console** do backend.

```java
System.out.println("📧 Código de verificação para: " + email);
System.out.println("🔐 Código: " + vc.getCode());
```

## Como integrar emails reais

### Opção 1: SendGrid (Recomendado - Gratuito até 100 emails/dia)

#### 1. Adicionar dependência no `pom.xml`:
```xml
<dependency>
    <groupId>com.sendgrid</groupId>
    <artifactId>sendgrid-java</artifactId>
    <version>4.9.3</version>
</dependency>
```

#### 2. Criar conta no SendGrid:
- Acesse: https://sendgrid.com/
- Crie uma conta gratuita
- Gere uma API Key em: Settings > API Keys

#### 3. Adicionar no `application.properties`:
```properties
sendgrid.api.key=SG.xxxxxxxxxxxxxxxxxxxxxxxx
sendgrid.from.email=noreply@brainwin.com
sendgrid.from.name=BrainWin
```

#### 4. Criar `EmailService.java`:
```java
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
    
    public void sendVerificationCode(String toEmail, String code) throws IOException {
        Email from = new Email(fromEmail, fromName);
        Email to = new Email(toEmail);
        String subject = "Seu código de verificação - BrainWin";
        
        String htmlContent = """
            <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
                <h2 style="color: #ff1b8d;">🎓 BrainWin</h2>
                <p>Olá!</p>
                <p>Seu código de verificação é:</p>
                <div style="background: #f0f0f0; padding: 20px; text-align: center; border-radius: 8px; margin: 20px 0;">
                    <h1 style="color: #14f195; letter-spacing: 8px; margin: 0;">%s</h1>
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
                throw new IOException("Erro ao enviar email: " + response.getBody());
            }
        } catch (IOException ex) {
            throw new IOException("Falha ao enviar email via SendGrid", ex);
        }
    }
    
    public void sendPasswordResetCode(String toEmail, String code) throws IOException {
        Email from = new Email(fromEmail, fromName);
        Email to = new Email(toEmail);
        String subject = "Recuperação de senha - BrainWin";
        
        String htmlContent = """
            <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
                <h2 style="color: #ff1b8d;">🔐 BrainWin</h2>
                <p>Olá!</p>
                <p>Você solicitou a recuperação de senha.</p>
                <p>Seu código de recuperação é:</p>
                <div style="background: #f0f0f0; padding: 20px; text-align: center; border-radius: 8px; margin: 20px 0;">
                    <h1 style="color: #14f195; letter-spacing: 8px; margin: 0;">%s</h1>
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
                throw new IOException("Erro ao enviar email: " + response.getBody());
            }
        } catch (IOException ex) {
            throw new IOException("Falha ao enviar email via SendGrid", ex);
        }
    }
}
```

#### 5. Atualizar `VerificationService.java`:
```java
@Autowired
private EmailService emailService;

public void sendEmailVerificationCode(String email) {
    // ... código existente ...
    
    codeRepo.save(vc);
    
    // SUBSTITUIR System.out.println por:
    try {
        emailService.sendVerificationCode(email, vc.getCode());
        System.out.println("✅ Email enviado para: " + email);
    } catch (IOException e) {
        System.err.println("❌ Erro ao enviar email: " + e.getMessage());
        // Opcional: logar erro ou retornar 500
    }
}
```

---

### Opção 2: AWS SES (Amazon Simple Email Service)

Mais robusto para produção, mas requer configuração da AWS.

#### 1. Dependência:
```xml
<dependency>
    <groupId>com.amazonaws</groupId>
    <artifactId>aws-java-sdk-ses</artifactId>
    <version>1.12.529</version>
</dependency>
```

#### 2. Configuração similar ao SendGrid

---

### Opção 3: Spring Mail (SMTP)

Funciona com Gmail, Outlook, etc.

#### 1. Dependência:
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-mail</artifactId>
</dependency>
```

#### 2. `application.properties`:
```properties
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=seuemail@gmail.com
spring.mail.password=suasenha_ou_app_password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

---

## 🎯 Recomendação

Para **desenvolvimento/testes**: Continue usando o console (atual)
Para **produção**: Use **SendGrid** (gratuito até 100 emails/dia, fácil de configurar)

---

## 📝 Notas Importantes

1. **Verificação de domínio**: Para emails de produção, você precisará de um domínio verificado
2. **Rate limiting**: Implemente limites para evitar spam (ex: 1 código a cada 60 segundos por email)
3. **Logs**: Sempre mantenha logs dos emails enviados para auditoria
4. **Fallback**: Tenha um plano B se o serviço de email falhar

