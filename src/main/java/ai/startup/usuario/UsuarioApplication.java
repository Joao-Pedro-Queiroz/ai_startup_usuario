package ai.startup.usuario;

import ai.startup.usuario.auth.JwtService;
import ai.startup.usuario.security.SecurityFilter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityRequirement;

@SpringBootApplication
public class UsuarioApplication {
    public static void main(String[] args) {
        SpringApplication.run(UsuarioApplication.class, args);
    }

    @Bean
    public FilterRegistrationBean<SecurityFilter> securityFilter(JwtService jwtService) {
        FilterRegistrationBean<SecurityFilter> reg = new FilterRegistrationBean<>();
        reg.setFilter(new SecurityFilter(jwtService)); // usa o que você já enviou
        reg.addUrlPatterns("/*");
        reg.setOrder(1);
        return reg;
    }

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
            .components(new Components().addSecuritySchemes(
                "bearerAuth",
                new SecurityScheme()
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")
            ))
            // <— isto ativa o botão Authorize no topo da UI
            .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }
}