package ai.startup.usuario.security;

import ai.startup.usuario.auth.JwtService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;

public class SecurityFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    // rotas liberadas (sem token)
    private static final Set<String> PUBLIC_PATHS = Set.of(
        "/auth/login",
        "/auth/register",          // <- registro público
        "/auth/send-verification-code",   // <- verificação de email
        "/auth/verify-email-code",        // <- verificação de email
        "/auth/forgot-password",          // <- recuperação de senha
        "/auth/reset-password",           // <- reset de senha
        "/v3/api-docs/**",
        "/swagger-ui/**",
        "/swagger-ui.html",
        "/actuator/health"
    );

    public SecurityFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

        // --- CORS básico ---
        addCors(response);
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        // --- público? ---
        if (isPublic(request)) {
            chain.doFilter(request, response);
            return;
        }

        // --- autenticação por Bearer ---
        String auth = request.getHeader("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing or invalid Authorization header");
            return;
        }

        Claims claims;
        try {
            String token = auth.substring(7);
            claims = jwtService.validar(token);
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid or expired token");
            return;
        }

        String email = claims.get("email", String.class);
        String permissao = claims.get("permissao", String.class);
        if (permissao == null) permissao = "USER";

        // deixa disponível para controller/service
        request.setAttribute("authEmail", email);
        request.setAttribute("authPermissao", permissao);

        // --- autorização simples ---
        if (requiresAdmin(request) && !"ADMIN".equalsIgnoreCase(permissao)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Admin permission required");
            return;
        }

        chain.doFilter(request, response);
    }

    private boolean isPublic(HttpServletRequest req) {
        String path = req.getRequestURI();
        for (String p : PUBLIC_PATHS) {
            if (pathMatcher.match(p, path)) return true;
        }
        return false;
    }

    // regra: DELETE /users/** requer ADMIN
    private boolean requiresAdmin(HttpServletRequest req) {
        String path = req.getRequestURI();
        String method = req.getMethod();

        // DELETE /users/** exige ADMIN
        if ("DELETE".equalsIgnoreCase(method) && pathMatcher.match("/users/**", path)) return true;

        // POST /users (criação padrão) exige ADMIN
        if ("POST".equalsIgnoreCase(method) && pathMatcher.match("/users", path)) return true;

        return false;
    }

    private void addCors(HttpServletResponse res) {
        res.setHeader("Access-Control-Allow-Origin", "*"); // ajuste para o domínio do seu front
        res.setHeader("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS");
        res.setHeader("Access-Control-Allow-Headers", "Authorization,Content-Type");
        res.setHeader("Access-Control-Expose-Headers", "Authorization");
        res.setHeader("Access-Control-Max-Age", "3600");
    }
}
