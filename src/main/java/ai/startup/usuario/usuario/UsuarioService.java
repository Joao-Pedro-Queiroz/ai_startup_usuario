package ai.startup.usuario.usuario;

import ai.startup.usuario.auth.AuthRequestDTO;
import ai.startup.usuario.auth.AuthResponseDTO;
import ai.startup.usuario.auth.JwtService;
import ai.startup.usuario.clients.PerfilClient;
import ai.startup.usuario.plano.UserPlan;
import ai.startup.usuario.plano.UserPlanMapper;
import ai.startup.usuario.plano.UserPlanRepository;
import ai.startup.usuario.support.TemplateLoader;
import ai.startup.usuario.privacy.ProfilePrivacy;
import ai.startup.usuario.privacy.ProfilePrivacyRepository;
import ai.startup.usuario.badge.BadgeRepository;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.ArrayList;

@Service
public class UsuarioService {

    private final UsuarioRepository repo;
    private final JwtService jwt;

    // NOVO: dependências para provisionar perfil e salvar plano localmente
    private final PerfilClient perfilClient;
    private final TemplateLoader templateLoader;
    private final UserPlanRepository userPlanRepo;
    private final ProfilePrivacyRepository privacyRepo;
    private final BadgeRepository badgeRepo;

    public UsuarioService(UsuarioRepository repo,
                          JwtService jwt,
                          PerfilClient perfilClient,
                          TemplateLoader templateLoader,
                          UserPlanRepository userPlanRepo,
                          ProfilePrivacyRepository privacyRepo,
                          BadgeRepository badgeRepo) {
        this.repo = repo;
        this.jwt = jwt;
        this.perfilClient = perfilClient;
        this.templateLoader = templateLoader;
        this.userPlanRepo = userPlanRepo;
        this.privacyRepo = privacyRepo;
        this.badgeRepo = badgeRepo;
    }
    
    /**
     * Atualiza o streak do usuário baseado no login diário
     */
    public Long updateStreakOnLogin(String userId) {
        Usuario user = repo.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));
        
        java.time.LocalDate today = java.time.LocalDate.now();
        java.time.LocalDate lastLogin = user.getUltimoLogin();
        Long currentStreak = user.getStreaks() != null ? user.getStreaks() : 0L;
        
        if (lastLogin == null) {
            // Primeiro login
            user.setStreaks(1L);
            user.setUltimoLogin(today);
        } else {
            long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(lastLogin, today);
            
            if (daysBetween == 0) {
                // Mesmo dia - mantém streak
                return currentStreak;
            } else if (daysBetween == 1) {
                // Login consecutivo - incrementa streak
                user.setStreaks(currentStreak + 1);
                user.setUltimoLogin(today);
            } else {
                // Quebrou o streak - reseta para 1
                user.setStreaks(1L);
                user.setUltimoLogin(today);
            }
        }
        
        Usuario saved = repo.save(user);
        return saved.getStreaks();
    }

    public UsuarioDTO criar(UsuarioCreateDTO dto) {
        if (dto == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Payload inválido.");
        }
        if (dto.email() != null && repo.existsByEmail(dto.email().toLowerCase())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "E-mail já cadastrado.");
        }
        if (dto.cpf() != null && repo.existsByCpf(normalizarCpf(dto.cpf()))) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "CPF já cadastrado.");
        }

        Usuario u = new Usuario();
        u.setNome(dto.nome());
        u.setSobrenome(dto.sobrenome());
        u.setCpf(normalizarCpf(dto.cpf()));
        u.setTelefone(dto.telefone());
        u.setNascimento(dto.nascimento());
        u.setEmail(dto.email() == null ? null : dto.email().toLowerCase());
        u.setSenhaHash(dto.senha() == null ? null : BCrypt.hashpw(dto.senha(), BCrypt.gensalt()));
        u.setWins(5L);
        u.setStreaks(0L);
        u.setXp(0L);
        u.setPermissao(dto.permissao() == null ? "USER" : dto.permissao().toUpperCase());
        u.setIsPremium(false); // Novo usuário começa como não-premium
        u.setExtendedTime(false); // Padrão: sem tempo estendido
        u.setSelectedPractice(null); // Ainda não selecionou uma prática

        return toDTO(repo.save(u));
    }

    /** Registro público: força permissao=USER + provisiona Perfil (API) + salva UserPlan (local) */
    public AuthResponseDTO registrar(UsuarioCreateDTO dto) {
        if (dto == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Payload inválido.");
        }
        if (dto.email() != null && repo.existsByEmail(dto.email().toLowerCase())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "E-mail já cadastrado.");
        }
        if (dto.cpf() != null && repo.existsByCpf(normalizarCpf(dto.cpf()))) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "CPF já cadastrado.");
        }

        Usuario u = new Usuario();
        u.setNome(dto.nome());
        u.setSobrenome(dto.sobrenome());
        u.setCpf(normalizarCpf(dto.cpf()));
        u.setTelefone(dto.telefone());
        u.setNascimento(dto.nascimento());
        u.setEmail(dto.email() == null ? null : dto.email().toLowerCase());
        u.setSenhaHash(dto.senha() == null ? null : BCrypt.hashpw(dto.senha(), BCrypt.gensalt()));
        u.setWins(5L);
        u.setStreaks(0L);
        u.setXp(0L);
        u.setPermissao("USER"); // <- força USER
        u.setIsPremium(false); // Novo usuário começa como não-premium
        u.setExtendedTime(false); // Padrão: sem tempo estendido
        u.setSelectedPractice(null); // Ainda não selecionou uma prática

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
        if (dto == null || dto.email() == null || dto.senha() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "E-mail e senha são obrigatórios.");
        }

        Usuario u = repo.findByEmail(dto.email().toLowerCase())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciais inválidas."));

        if (u.getSenhaHash() == null || !BCrypt.checkpw(dto.senha(), u.getSenhaHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciais inválidas.");
        }

        return new AuthResponseDTO(jwt.gerarToken(u), "Bearer");
    }

    public UsuarioDTO obter(String id) {
        return repo.findById(id).map(this::toDTO)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado."));
    }

    public UsuarioDTO obterPorEmail(String email) {
        if (email == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "E-mail é obrigatório.");
        }
        return repo.findByEmail(email.toLowerCase())
                .map(this::toDTO)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado."));
    }

    public List<UsuarioDTO> listar() {
        return repo.findAll().stream().map(this::toDTO).toList();
    }

    public UsuarioDTO atualizar(String id, UsuarioUpdateDTO dto, String authPermissao) {
        var u = repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado."));

        if (dto == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Payload inválido.");
        }

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
        if (dto.extendedTime() != null) u.setExtendedTime(dto.extendedTime());
        if (dto.selectedPractice() != null) u.setSelectedPractice(dto.selectedPractice());

        if (dto.permissao() != null) {
            if (!"ADMIN".equalsIgnoreCase(authPermissao)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Apenas ADMIN pode alterar a permissão.");
            }
            u.setPermissao(dto.permissao().toUpperCase());
        }

        return toDTO(repo.save(u));
    }

    public void deletar(String id) {
        if (!repo.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado.");
        }
        repo.deleteById(id);
    }

    /**
     * Reseta a senha de um usuário (usado na recuperação de senha)
     */
    public void resetPassword(String userId, String newPassword) {
        if (newPassword == null || newPassword.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nova senha é obrigatória");
        }
        
        Usuario u = repo.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));
        
        u.setSenhaHash(BCrypt.hashpw(newPassword, BCrypt.gensalt()));
        repo.save(u);
    }

    /**
     * Busca perfil público de um usuário (filtrando dados privados)
     */
    public PublicProfileDTO getPublicProfile(String userId) {
        Usuario u = repo.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));
        
        // Busca configurações de privacidade (usa padrão se não existir)
        ProfilePrivacy privacy = privacyRepo.findByUserId(userId)
                .orElse(ProfilePrivacy.createDefault(userId));
        
        // Busca badges conquistados se públicos
        List<String> badgeIds = new ArrayList<>();
        if (privacy.getBadgesPublic()) {
            badgeIds = badgeRepo.findByUserIdAndEarnedAtIsNotNull(userId)
                    .stream()
                    .map(b -> b.getBadgeId())
                    .toList();
        }
        
        return new PublicProfileDTO(
                u.getId(),
                u.getNome(),
                u.getSobrenome(),
                privacy.getEmailPublic() ? u.getEmail() : null,
                privacy.getTelefonePublic() ? u.getTelefone() : null,
                privacy.getWinsPublic() ? u.getWins() : null,
                privacy.getStreaksPublic() ? u.getStreaks() : null,
                privacy.getXpPublic() ? u.getXp() : null,
                null, // bestScore calculado no frontend
                null, // simuladosCount calculado no frontend
                badgeIds
        );
    }

    /**
     * Busca ranking de usuários ordenados por XP ou Streak
     */
    public List<UsuarioDTO> getRankingByXp(int limit) {
        Sort sort = Sort.by(Sort.Direction.DESC, "xp", "streaks");
        List<Usuario> users = repo.findAll(sort);
        
        // Limita e converte para DTO
        return users.stream()
                .limit(limit > 0 ? limit : 100) // Max 100
                .map(this::toDTO)
                .toList();
    }

    /**
     * Busca ranking de usuários ordenados por Streak
     */
    public List<UsuarioDTO> getRankingByStreak(int limit) {
        Sort sort = Sort.by(Sort.Direction.DESC, "streaks", "xp");
        List<Usuario> users = repo.findAll(sort);
        
        // Limita e converte para DTO
        return users.stream()
                .limit(limit > 0 ? limit : 100) // Max 100
                .map(this::toDTO)
                .toList();
    }

    /**
     * Faz upgrade para premium (custa 100 wins)
     */
    public UsuarioDTO upgradeToPremium(String email) {
        Usuario user = repo.findByEmail(email.toLowerCase())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));
        
        // Verifica se já é premium
        if (user.getIsPremium() != null && user.getIsPremium()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Usuário já é premium");
        }
        
        // Verifica se tem saldo suficiente (100 wins)
        long currentWins = user.getWins() != null ? user.getWins() : 0L;
        if (currentWins < 100L) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                "Saldo insuficiente. Necessário: 100 wins. Atual: " + currentWins + " wins");
        }
        
        // Debita 100 wins e ativa premium
        user.setWins(currentWins - 100L);
        user.setIsPremium(true);
        
        return toDTO(repo.save(user));
    }

    // helpers
    private String normalizarCpf(String cpf) {
        return cpf == null ? null : cpf.replaceAll("\\D+", "");
    }
    private UsuarioDTO toDTO(Usuario u) {
        return new UsuarioDTO(
                u.getId(), u.getNome(), u.getSobrenome(), u.getCpf(), u.getTelefone(),
                u.getNascimento(), u.getEmail(), u.getWins(), u.getStreaks(), u.getXp(), u.getPermissao(),
                u.getIsPremium() != null ? u.getIsPremium() : false,
                u.getExtendedTime() != null ? u.getExtendedTime() : false,
                u.getSelectedPractice()
        );
    }
}
