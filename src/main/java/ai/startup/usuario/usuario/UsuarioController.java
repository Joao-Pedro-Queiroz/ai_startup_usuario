package ai.startup.usuario.usuario;

import ai.startup.usuario.auth.AuthRequestDTO;
import ai.startup.usuario.auth.AuthResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import java.util.List;

@RestController
@RequestMapping
public class UsuarioController {

    private final UsuarioService service;

    public UsuarioController(UsuarioService service) {
        this.service = service;
    }

    // AUTH
    @Operation(security = {})  
    @PostMapping("/auth/login")
    public ResponseEntity<AuthResponseDTO> login(@RequestBody AuthRequestDTO req) {
        return ResponseEntity.ok(service.autenticar(req));
    }

    @Operation(security = {}) // público na UI do Swagger
    @PostMapping("/auth/register")
    public ResponseEntity<AuthResponseDTO> register(@RequestBody UsuarioCreateDTO dto) {
        return ResponseEntity.ok(service.registrar(dto));
    }

    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/users/me")
    public ResponseEntity<UsuarioDTO> me(HttpServletRequest req) {
        String email = (String) req.getAttribute("authEmail"); // vindo do SecurityFilter
        if (email == null || email.isBlank()) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(service.obterPorEmail(email));
    }

    // CRUD
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/users")
    public ResponseEntity<UsuarioDTO> criar(@RequestBody UsuarioCreateDTO dto) {
        return ResponseEntity.ok(service.criar(dto));
    }

    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/users")
    public ResponseEntity<List<UsuarioDTO>> listar() {
        return ResponseEntity.ok(service.listar());
    }

    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/users/{id}")
    public ResponseEntity<UsuarioDTO> obter(@PathVariable String id) {
        return ResponseEntity.ok(service.obter(id));
    }

    // update parcial: envie só os campos que quer mudar
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/users/{id}")
    public ResponseEntity<UsuarioDTO> atualizar(@PathVariable String id,
                                                @RequestBody UsuarioUpdateDTO dto,
                                                HttpServletRequest req) {
        String authPermissao = (String) req.getAttribute("authPermissao"); // setado no SecurityFilter
        return ResponseEntity.ok(service.atualizar(id, dto, authPermissao));
    }

    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deletar(@PathVariable String id) {
        service.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
