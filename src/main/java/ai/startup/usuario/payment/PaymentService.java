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

        // BrainWin Learn - R$ 59,90/mês
        PRODUCTS.put("brainwin-learn", new ProductInfo(
            "BrainWin Learn - Assinatura Mensal",
            5990L, // 59.90 em centavos
            "brl",
            0,  // não dá wins
            true // é assinatura
        ));
    }

    public PaymentService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
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
        Session session = Session.retrieve(sessionId);
        
        String userId = session.getMetadata().get("userId");
        String productId = session.getMetadata().get("productId");
        
        if (userId == null || productId == null) {
            throw new IllegalArgumentException("Metadata inválida na sessão");
        }

        Usuario user = usuarioRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado: " + userId));

        ProductInfo product = PRODUCTS.get(productId);
        if (product == null) {
            throw new IllegalArgumentException("Produto não encontrado: " + productId);
        }

        if (product.isSubscription) {
            // Ativar assinatura BrainWin Learn
            user.setIsPremium(true);
            // Aqui você pode adicionar campos extras para rastrear a assinatura
            System.out.println("[Payment] Assinatura ativada para usuário: " + user.getEmail());
        } else {
            // Adicionar wins
            long currentWins = user.getWins() != null ? user.getWins() : 0L;
            user.setWins(currentWins + product.wins);
            System.out.println("[Payment] " + product.wins + " wins adicionados para usuário: " + user.getEmail());
        }

        usuarioRepository.save(user);
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

