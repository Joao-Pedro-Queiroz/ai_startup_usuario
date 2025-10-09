package ai.startup.usuario.usuario;

import ai.startup.usuario.auth.AuthRequestDTO;
import ai.startup.usuario.auth.AuthResponseDTO;
import ai.startup.usuario.auth.JwtService;
import ai.startup.usuario.clients.PerfilClient;
import ai.startup.usuario.plano.UserPlan;
import ai.startup.usuario.plano.UserPlanMapper;
import ai.startup.usuario.plano.UserPlanRepository;
import ai.startup.usuario.support.TemplateLoader;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class UsuarioService {

    private final UsuarioRepository repo;
    private final JwtService jwt;

    // NOVO: dependências para provisionar perfil e salvar plano localmente
    private final PerfilClient perfilClient;
    private final TemplateLoader templateLoader;
    private final UserPlanRepository userPlanRepo;

    public UsuarioService(UsuarioRepository repo,
                          JwtService jwt,
                          PerfilClient perfilClient,
                          TemplateLoader templateLoader,
                          UserPlanRepository userPlanRepo) {
        this.repo = repo;
        this.jwt = jwt;
        this.perfilClient = perfilClient;
        this.templateLoader = templateLoader;
        this.userPlanRepo = userPlanRepo;
    }

    public UsuarioDTO criar(UsuarioCreateDTO dto) {
        if (dto.email() != null && repo.existsByEmail(dto.email().toLowerCase()))
            throw new RuntimeException("E-mail já cadastrado.");
        if (dto.cpf() != null && repo.existsByCpf(normalizarCpf(dto.cpf())))
            throw new RuntimeException("CPF já cadastrado.");

        Usuario u = new Usuario();
        u.setNome(dto.nome());
        u.setSobrenome(dto.sobrenome());
        u.setCpf(normalizarCpf(dto.cpf()));
        u.setTelefone(dto.telefone());
        u.setNascimento(dto.nascimento());
        u.setEmail(dto.email() == null ? null : dto.email().toLowerCase());
        u.setSenhaHash(dto.senha() == null ? null : BCrypt.hashpw(dto.senha(), BCrypt.gensalt()));
        u.setWins(0L);
        u.setStreaks(0L);
        u.setXp(0L);
        u.setPermissao(dto.permissao() == null ? "USER" : dto.permissao().toUpperCase());

        return toDTO(repo.save(u));
    }

    /** Registro público: força permissao=USER + provisiona Perfil (API) + salva UserPlan (local) */
    public AuthResponseDTO registrar(UsuarioCreateDTO dto) {
        if (dto.email() != null && repo.existsByEmail(dto.email().toLowerCase()))
            throw new RuntimeException("E-mail já cadastrado.");
        if (dto.cpf() != null && repo.existsByCpf(normalizarCpf(dto.cpf())))
            throw new RuntimeException("CPF já cadastrado.");

        Usuario u = new Usuario();
        u.setNome(dto.nome());
        u.setSobrenome(dto.sobrenome());
        u.setCpf(normalizarCpf(dto.cpf()));
        u.setTelefone(dto.telefone());
        u.setNascimento(dto.nascimento());
        u.setEmail(dto.email() == null ? null : dto.email().toLowerCase());
        u.setSenhaHash(dto.senha() == null ? null : BCrypt.hashpw(dto.senha(), BCrypt.gensalt()));
        u.setWins(0L);
        u.setStreaks(0L);
        u.setXp(0L);
        u.setPermissao("USER"); // <- força USER

        Usuario salvo = repo.save(u);

        // Gera JWT (ainda não retorna)
        String token = jwt.gerarToken(salvo);
        String bearer = "Bearer " + token;

        // 1) Cria PERFIL na API de Perfil usando o template (sem created_at/updated_at)
        try {
            var perfilBody = templateLoader.loadProfileTemplate(salvo.getId());
            perfilClient.criarPerfil(bearer, perfilBody);
        } catch (Exception e) {
            // política: falhou provisionamento -> reverte cadastro (ou só loga; aqui vou abortar com 502)
            repo.deleteById(salvo.getId());
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Falha ao provisionar Perfil", e);
        }

        // 2) Salva USER PLAN localmente (não existe API externa para plano)
        try {
            var planBody = templateLoader.loadUserPlanTemplate(salvo.getId());
            UserPlan plan = UserPlanMapper.fromTemplateMap(planBody);
            // evita duplicar se houver tentativa repetida (não deve ocorrer no register, mas fica seguro)
            userPlanRepo.findByUserId(salvo.getId()).ifPresentOrElse(
                __ -> {}, () -> userPlanRepo.save(plan)
            );
        } catch (Exception e) {
            // se o plano falhar, aqui também reverto o usuário para manter consistência
            repo.deleteById(salvo.getId());
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Falha ao salvar UserPlan", e);
        }

        // Sucesso: devolve o token
        return new AuthResponseDTO(token, "Bearer");
    }

    public AuthResponseDTO autenticar(AuthRequestDTO dto) {
        Usuario u = repo.findByEmail(dto.email().toLowerCase())
                .orElseThrow(() -> new RuntimeException("Credenciais inválidas."));

        if (u.getSenhaHash() == null || !BCrypt.checkpw(dto.senha(), u.getSenhaHash()))
            throw new RuntimeException("Credenciais inválidas.");

        return new AuthResponseDTO(jwt.gerarToken(u), "Bearer");
    }

    public UsuarioDTO obter(String id) {
        return repo.findById(id).map(this::toDTO)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado."));
    }

    public UsuarioDTO obterPorEmail(String email) {
        return repo.findByEmail(email.toLowerCase())
                .map(this::toDTO)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado."));
    }

    public List<UsuarioDTO> listar() {
        return repo.findAll().stream().map(this::toDTO).toList();
    }

    public UsuarioDTO atualizar(String id, UsuarioUpdateDTO dto, String authPermissao) {
        Usuario u = repo.findById(id).orElseThrow(() -> new RuntimeException("Usuário não encontrado."));

        if (dto.nome() != null) u.setNome(dto.nome());
        if (dto.sobrenome() != null) u.setSobrenome(dto.sobrenome());
        if (dto.telefone() != null) u.setTelefone(dto.telefone());
        if (dto.nascimento() != null) u.setNascimento(dto.nascimento());
        if (dto.email() != null) u.setEmail(dto.email().toLowerCase());
        if (dto.cpf() != null) u.setCpf(normalizarCpf(dto.cpf()));
        if (dto.senha() != null) u.setSenhaHash(BCrypt.hashpw(dto.senha(), BCrypt.gensalt()));
        if (dto.wins() != null) u.setWins(dto.wins());
        if (dto.streaks() != null) u.setStreaks(dto.streaks());
        if (dto.xp() != null) u.setXp(dto.xp());

        if (dto.permissao() != null) {
            if (!"ADMIN".equalsIgnoreCase(authPermissao))
                throw new RuntimeException("Apenas ADMIN pode alterar a permissão.");
            u.setPermissao(dto.permissao().toUpperCase());
        }

        return toDTO(repo.save(u));
    }

    public void deletar(String id) {
        if (!repo.existsById(id)) throw new RuntimeException("Usuário não encontrado.");
        repo.deleteById(id);
    }

    // helpers
    private String normalizarCpf(String cpf) {
        return cpf == null ? null : cpf.replaceAll("\\D+", "");
    }
    private UsuarioDTO toDTO(Usuario u) {
        return new UsuarioDTO(
                u.getId(), u.getNome(), u.getSobrenome(), u.getCpf(), u.getTelefone(),
                u.getNascimento(), u.getEmail(), u.getWins(), u.getStreaks(), u.getXp(), u.getPermissao()
        );
    }
}
