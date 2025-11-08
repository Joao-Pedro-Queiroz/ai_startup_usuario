package ai.startup.usuario.payment;

import com.stripe.exception.StripeException;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.StripeObject;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentService paymentService;

    @Value("${stripe.webhook.secret}")
    private String webhookSecret;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    /**
     * POST /payments/create-checkout-session
     * Cria uma sessão de checkout do Stripe
     */
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/create-checkout-session")
    public ResponseEntity<CheckoutResponseDTO> createCheckoutSession(
        @RequestBody CheckoutRequestDTO request,
        HttpServletRequest httpRequest
    ) {
        try {
            // Obter email do usuário autenticado (do SecurityFilter)
            String userEmail = (String) httpRequest.getAttribute("authEmail");
            if (userEmail == null || userEmail.isBlank()) {
                return ResponseEntity.status(401).build();
            }

            CheckoutResponseDTO response = paymentService.createCheckoutSession(
                request.productId(),
                userEmail,
                request.successUrl(),
                request.cancelUrl()
            );

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (StripeException e) {
            System.err.println("[Payment] Erro ao criar sessão de checkout: " + e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * GET /payments/success?session_id={CHECKOUT_SESSION_ID}
     * Confirma pagamento após redirecionamento do Stripe
     */
    @Operation(security = {})  // Público
    @GetMapping("/success")
    public ResponseEntity<String> paymentSuccess(@RequestParam("session_id") String sessionId) {
        try {
            paymentService.handleSuccessfulPayment(sessionId);
            return ResponseEntity.ok("Pagamento processado com sucesso!");
        } catch (Exception e) {
            System.err.println("[Payment] Erro ao processar sucesso: " + e.getMessage());
            return ResponseEntity.status(500).body("Erro ao processar pagamento");
        }
    }

    /**
     * POST /payments/webhook
     * Webhook do Stripe para eventos de pagamento
     * 
     * IMPORTANTE: Esta rota deve estar configurada no Stripe Dashboard
     * URL: https://seu-dominio.com/payments/webhook
     */
    @Operation(security = {})  // Público (vem do Stripe)
    @PostMapping("/webhook")
    public ResponseEntity<String> handleStripeWebhook(
        @RequestBody String payload,
        @RequestHeader("Stripe-Signature") String sigHeader
    ) {
        Event event;

        try {
            // Verificar assinatura do webhook (segurança)
            event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
        } catch (SignatureVerificationException e) {
            System.err.println("[Payment Webhook] Assinatura inválida: " + e.getMessage());
            return ResponseEntity.status(400).body("Invalid signature");
        }

        // Processar eventos do Stripe
        System.out.println("[Payment Webhook] Evento recebido: " + event.getType());

        try {
            switch (event.getType()) {
                case "checkout.session.completed":
                    // Pagamento único ou assinatura iniciada
                    Session session = (Session) deserializeEvent(event);
                    String sessionId = session.getId();
                    System.out.println("[Payment Webhook] Checkout completo: " + sessionId);
                    paymentService.handleSuccessfulPayment(sessionId);
                    break;

                case "invoice.payment_succeeded":
                    // Renovação de assinatura bem-sucedida
                    System.out.println("[Payment Webhook] Renovação de assinatura bem-sucedida");
                    // TODO: Implementar lógica de renovação se necessário
                    break;

                case "customer.subscription.deleted":
                    // Assinatura cancelada
                    System.out.println("[Payment Webhook] Assinatura cancelada");
                    // TODO: Implementar lógica de cancelamento
                    break;

                default:
                    System.out.println("[Payment Webhook] Evento não tratado: " + event.getType());
            }

            return ResponseEntity.ok("Webhook processado");
        } catch (Exception e) {
            System.err.println("[Payment Webhook] Erro ao processar evento: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body("Erro ao processar webhook");
        }
    }

    /**
     * Helper para deserializar eventos do Stripe
     */
    private StripeObject deserializeEvent(Event event) {
        EventDataObjectDeserializer dataObjectDeserializer = event.getDataObjectDeserializer();
        if (dataObjectDeserializer.getObject().isPresent()) {
            return dataObjectDeserializer.getObject().get();
        }
        throw new IllegalArgumentException("Não foi possível deserializar o evento");
    }
}

