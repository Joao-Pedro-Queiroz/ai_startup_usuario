package ai.startup.usuario.clients;

import org.springframework.http.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
public class PerfilClient {
    private final RestTemplate rt = new RestTemplate();
    private final String base;

    public PerfilClient(@Value("${api.perfil.base}") String base) {
        this.base = base;
    }

    public void criarPerfil(String bearerToken, Map<String,Object> body) {
        var url = base + "/perfis";
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", bearerToken);
        ResponseEntity<Void> resp = rt.exchange(url, HttpMethod.POST, new HttpEntity<>(body, headers), Void.class);
        if (!resp.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Falha ao criar perfil: " + resp.getStatusCode());
        }
    }
}
