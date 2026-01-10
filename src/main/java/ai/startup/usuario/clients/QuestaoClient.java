package ai.startup.usuario.clients;

import org.springframework.http.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Component
public class QuestaoClient {
    private final RestTemplate rt = new RestTemplate();
    private final String base;

    public QuestaoClient(@Value("${api.questao.base}") String base) {
        this.base = base;
    }

    /**
     * Busca todas as questões de um usuário
     */
    public List<Map<String, Object>> getQuestoesByUsuario(String bearerToken, String userId) {
        var url = base + "/questoes/by-usuario/" + userId;
        var headers = new HttpHeaders();
        headers.set("Authorization", bearerToken);
        
        try {
            ResponseEntity<List> resp = rt.exchange(
                url, 
                HttpMethod.GET, 
                new HttpEntity<>(headers), 
                List.class
            );
            
            if (resp.getStatusCode().is2xxSuccessful() && resp.getBody() != null) {
                return (List<Map<String, Object>>) resp.getBody();
            }
            return List.of();
        } catch (Exception e) {
            System.err.println("[QuestaoClient] Erro ao buscar questões do usuário " + userId + ": " + e.getMessage());
            return List.of();
        }
    }

    /**
     * Conta questões corretas de um usuário
     * Uma questão é considerada correta se alternativa_marcada == correct_option
     */
    public long countCorrectAnswers(String bearerToken, String userId) {
        List<Map<String, Object>> questoes = getQuestoesByUsuario(bearerToken, userId);
        
        return questoes.stream()
            .filter(q -> {
                Object marcada = q.get("alternativa_marcada");
                Object correta = q.get("correct_option");
                
                if (marcada == null || correta == null) {
                    return false;
                }
                
                // Compara como strings (case-insensitive)
                String marcadaStr = String.valueOf(marcada).trim().toUpperCase();
                String corretaStr = String.valueOf(correta).trim().toUpperCase();
                
                return marcadaStr.equals(corretaStr);
            })
            .count();
    }
}

