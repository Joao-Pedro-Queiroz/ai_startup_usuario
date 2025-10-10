// src/main/java/ai/startup/usuario/plano/UserPlanMapper.java
package ai.startup.usuario.plano;

import java.util.Map;

@SuppressWarnings("unchecked")
public class UserPlanMapper {
    public static UserPlan fromTemplateMap(Map<String,Object> tpl) {
        return UserPlan.builder()
                .userId((String) tpl.get("user_id"))
                .topics((Map<String,Object>) tpl.get("topics"))
                .build();
    }
}
