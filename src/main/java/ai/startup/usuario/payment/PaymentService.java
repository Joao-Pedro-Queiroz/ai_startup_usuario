package ai.startup.usuario.payment;

import ai.startup.usuario.usuario.Usuario;
import ai.startup.usuario.usuario.UsuarioRepository;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class PaymentService {

    private final UsuarioRepository usuarioRepository;
    private final ProcessedPaymentRepository processedPaymentRepository;

    @Value("${stripe.webhook.secret}")
    private String webhookSecret;

    // Produtos disponíveis
    private static final Map<String, ProductInfo> PRODUCTS = new HashMap<>();

    static {
        // Pacote de Wins - R$ 19,90
        PRODUCTS.put("wins-pack", new ProductInfo(
            "Pacote de Wins",
            1990L, // 19.90 em centavos
            "brl",
            50,  // wins
            false // não é assinatura
        ));

        // BrainWin Learn - R$ 99,99/mês
        PRODUCTS.put("brainwin-learn", new ProductInfo(
            "BrainWin Learn - Assinatura Mensal",
            9999L, // 99.99 em centavos
            "brl",
            0,  // não dá wins
            true // é assinatura
        ));
    }

    public PaymentService(UsuarioRepository usuarioRepository, ProcessedPaymentRepository processedPaymentRepository) {
        this.usuarioRepository = usuarioRepository;
        this.processedPaymentRepository = processedPaymentRepository;
    }

    /**
     * Cria uma sessão de checkout do Stripe
     */
    public CheckoutResponseDTO createCheckoutSession(
        String productId,
        String userEmail,
        String successUrl,
        String cancelUrl
    ) throws StripeException {
        
        ProductInfo product = PRODUCTS.get(productId);
        if (product == null) {
            throw new IllegalArgumentException("Produto não encontrado: " + productId);
        }

        // Buscar usuário para obter o ID
        Usuario user = usuarioRepository.findByEmail(userEmail)
            .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

        // Validar email antes de enviar para o Stripe
        if (!isValidEmail(userEmail)) {
            System.out.println("[Payment] ❌ Email inválido detectado: " + userEmail);
            throw new IllegalArgumentException(
                "Email inválido: " + userEmail + 
                ". Por favor, atualize seu email no perfil antes de fazer uma compra."
            );
        }

        SessionCreateParams.Builder builder = SessionCreateParams.builder()
            .setMode(product.isSubscription ? 
                SessionCreateParams.Mode.SUBSCRIPTION : 
                SessionCreateParams.Mode.PAYMENT)
            .setSuccessUrl(successUrl + "?session_id={CHECKOUT_SESSION_ID}")
            .setCancelUrl(cancelUrl)
            .setCustomerEmail(userEmail)
            .putMetadata("userId", user.getId())
            .putMetadata("productId", productId);

        if (product.isSubscription) {
            // Modo assinatura
            builder.addLineItem(
                SessionCreateParams.LineItem.builder()
                    .setPriceData(
                        SessionCreateParams.LineItem.PriceData.builder()
                            .setCurrency(product.currency)
                            .setUnitAmount(product.priceInCents)
                            .setRecurring(
                                SessionCreateParams.LineItem.PriceData.Recurring.builder()
                                    .setInterval(SessionCreateParams.LineItem.PriceData.Recurring.Interval.MONTH)
                                    .build()
                            )
                            .setProductData(
                                SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                    .setName(product.name)
                                    .setDescription("Acesso ilimitado a soluções e dicas por mês")
                                    .build()
                            )
                            .build()
                    )
                    .setQuantity(1L)
                    .build()
            );
        } else {
            // Modo pagamento único
            builder.addLineItem(
                SessionCreateParams.LineItem.builder()
                    .setPriceData(
                        SessionCreateParams.LineItem.PriceData.builder()
                            .setCurrency(product.currency)
                            .setUnitAmount(product.priceInCents)
                            .setProductData(
                                SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                    .setName(product.name)
                                    .setDescription(product.wins + " Wins para usar no BrainWin")
                                    .build()
                            )
                            .build()
                    )
                    .setQuantity(1L)
                    .build()
            );
        }

        Session session = Session.create(builder.build());

        return new CheckoutResponseDTO(session.getId(), session.getUrl());
    }

    /**
     * Processa pagamento bem-sucedido
     */
    public void handleSuccessfulPayment(String sessionId) throws StripeException {
        System.out.println("[Payment] ========================================");
        System.out.println("[Payment] Processando pagamento para sessão: " + sessionId);
        
        // Verificar se já foi processado no banco (evitar duplicação)
        if (processedPaymentRepository.existsBySessionId(sessionId)) {
            System.out.println("[Payment] ⚠️⚠️⚠️ DUPLICAÇÃO DETECTADA! ⚠️⚠️⚠️");
            System.out.println("[Payment] Sessão " + sessionId + " já foi processada anteriormente!");
            System.out.println("[Payment] IGNORANDO para evitar adicionar wins duplicados.");
            System.out.println("[Payment] ========================================");
            return;
        }
        
        Session session = Session.retrieve(sessionId);
        System.out.println("[Payment] Sessão recuperada do Stripe: " + session.getId());
        System.out.println("[Payment] Status da sessão: " + session.getPaymentStatus());
        
        String userId = session.getMetadata().get("userId");
        String productId = session.getMetadata().get("productId");
        
        System.out.println("[Payment] Metadata - userId: " + userId + ", productId: " + productId);
        
        if (userId == null || productId == null) {
            System.err.println("[Payment] ERRO: Metadata inválida na sessão");
            throw new IllegalArgumentException("Metadata inválida na sessão");
        }

        Usuario user = usuarioRepository.findById(userId)
            .orElseThrow(() -> {
                System.err.println("[Payment] ERRO: Usuário não encontrado: " + userId);
                return new IllegalArgumentException("Usuário não encontrado: " + userId);
            });

        System.out.println("[Payment] Usuário encontrado: " + user.getEmail() + " (wins atuais: " + user.getWins() + ")");

        ProductInfo product = PRODUCTS.get(productId);
        if (product == null) {
            System.err.println("[Payment] ERRO: Produto não encontrado: " + productId);
            throw new IllegalArgumentException("Produto não encontrado: " + productId);
        }

        System.out.println("[Payment] Produto: " + product.name + " (wins: " + product.wins + ", subscription: " + product.isSubscription + ")");

        if (product.isSubscription) {
            // Ativar assinatura BrainWin Learn
            user.setIsPremium(true);
            System.out.println("[Payment] ✅ Assinatura ativada para usuário: " + user.getEmail());
        } else {
            // Adicionar wins
            long currentWins = user.getWins() != null ? user.getWins() : 0L;
            long newWins = currentWins + product.wins;
            user.setWins(newWins);
            System.out.println("[Payment] ✅ Wins atualizados: " + currentWins + " → " + newWins + " (+" + product.wins + ")");
        }

        usuarioRepository.save(user);
        System.out.println("[Payment] ✅ Usuário salvo no banco de dados com sucesso!");
        
        // Marcar sessão como processada NO BANCO (persistente)
        ProcessedPayment processedPayment = new ProcessedPayment(sessionId, userId, productId);
        processedPaymentRepository.save(processedPayment);
        System.out.println("[Payment] 🔒 Sessão salva no banco como processada (proteção permanente contra duplicação)");
        
        // Verificar se foi salvo corretamente
        Usuario userVerify = usuarioRepository.findById(userId).orElse(null);
        if (userVerify != null) {
            System.out.println("[Payment] ✅ VERIFICAÇÃO FINAL: Usuário " + userVerify.getEmail() + " agora tem " + userVerify.getWins() + " wins");
        }
        
        long totalProcessed = processedPaymentRepository.count();
        System.out.println("[Payment] 📊 Total de pagamentos processados no sistema: " + totalProcessed);
        System.out.println("[Payment] ========================================");
    }

    /**
     * Valida formato de email
     */
    private boolean isValidEmail(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        // Regex simples para validação de email
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return email.matches(emailRegex);
    }

    // Classe interna para info de produtos
    private static class ProductInfo {
        String name;
        Long priceInCents;
        String currency;
        int wins;
        boolean isSubscription;

        ProductInfo(String name, Long priceInCents, String currency, int wins, boolean isSubscription) {
            this.name = name;
            this.priceInCents = priceInCents;
            this.currency = currency;
            this.wins = wins;
            this.isSubscription = isSubscription;
        }
    }
}

