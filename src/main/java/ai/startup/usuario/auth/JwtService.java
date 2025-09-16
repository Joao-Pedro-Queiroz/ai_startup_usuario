package ai.startup.usuario.auth;

import ai.startup.usuario.usuario.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

@Service
public class JwtService {

    private final Key key;

    public JwtService(@Value("${jwt.secret}") String secretBase64) {
        if (secretBase64 == null || secretBase64.isBlank()) {
            throw new IllegalStateException("JWT secret not configured. Set env JWT_SECRET.");
        }
        byte[] bytes = Decoders.BASE64.decode(secretBase64);
        if (bytes.length < 32) { // 256 bits
            throw new IllegalStateException("JWT secret too short. Use Base64 with >= 32 bytes.");
        }
        this.key = Keys.hmacShaKeyFor(bytes);
    }

    public String gerarToken(Usuario u) {
        Instant now = Instant.now();
        return Jwts.builder()
                .setSubject(u.getEmail())
                .addClaims(Map.of(
                        "email", u.getEmail(),
                        "permissao", u.getPermissao() == null ? "USER" : u.getPermissao()
                ))
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusSeconds(6 * 60 * 60))) // 6h
                .signWith(key)
                .compact();
    }

    public Claims validar(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build()
                .parseClaimsJws(token).getBody();
    }
}