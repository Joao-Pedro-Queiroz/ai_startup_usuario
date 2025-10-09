package ai.startup.usuario.support;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.Map;

@Component
public class TemplateLoader {
    private final ObjectMapper om = new ObjectMapper();

    public Map<String,Object> loadProfileTemplate(String userId) {
        try (InputStream in = new ClassPathResource("templates/profile_template.json").getInputStream()) {
            Map<String,Object> map = om.readValue(in, new TypeReference<Map<String,Object>>(){});
            map.put("user_id", userId);
            // NÃO setar created_at/updated_at aqui
            return map;
        } catch (Exception e) {
            throw new RuntimeException("Erro lendo profile_template.json", e);
        }
    }

    public Map<String,Object> loadUserPlanTemplate(String userId) {
        try (InputStream in = new ClassPathResource("templates/user_plan_template.json").getInputStream()) {
            Map<String,Object> map = om.readValue(in, new TypeReference<Map<String,Object>>(){});
            map.put("user_id", userId);
            // NÃO alterar updated_at (deixa o valor do template)
            return map;
        } catch (Exception e) {
            throw new RuntimeException("Erro lendo user_plan_template.json", e);
        }
    }
}
