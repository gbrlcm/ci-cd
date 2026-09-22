package br.com.inovagab.service;

import br.com.inovagab.dto.request.LoginRequest;
import br.com.inovagab.dto.request.RegisterRequest;
import br.com.inovagab.dto.response.AuthResponse;
import br.com.inovagab.model.Usuario;
import br.com.inovagab.repository.UsuarioRepository;
import br.com.inovagab.repository.GrupoRepository;
import br.com.inovagab.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private GrupoRepository grupoRepository;

    public AuthResponse authenticateUser(LoginRequest loginRequest) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(loginRequest.getEmail());
        
        if (usuarioOpt.isEmpty()) {
            throw new RuntimeException("Usuário não encontrado.");
        }

        Usuario usuario = usuarioOpt.get();
        
        boolean senhaCorreta = false;
        String senhaArmazenada = usuario.getSenha();

        if (senhaArmazenada != null) {
            if (isBCryptHash(senhaArmazenada)) {
                senhaCorreta = passwordEncoder.matches(loginRequest.getPassword(), senhaArmazenada);
            } else {
                // Senha legada em texto puro (ex: criada diretamente no MongoDB Compass)
                if (senhaArmazenada.equals(loginRequest.getPassword())) {
                    senhaCorreta = true;
                    // Auto-migra para hash BCrypt no banco
                    usuario.setSenha(passwordEncoder.encode(loginRequest.getPassword()));
                    usuarioRepository.save(usuario);
                }
            }
        }

        if (!senhaCorreta) {
            throw new RuntimeException("Senha inválida.");
        }
        
        if (usuario.getRole() == null || !usuario.getRole().equalsIgnoreCase(loginRequest.getSelectedProfile())) {
            throw new RuntimeException("Acesso negado: Seu perfil é " + usuario.getRole());
        }

        String token = tokenProvider.generateToken(usuario.getId(), usuario.getRole(), usuario.getGroupId());
        return new AuthResponse(
                token,
                usuario.getId(),
                usuario.getRole(),
                usuario.getNome(),
                usuario.getSobrenome(),
                usuario.getUnidade(),
                usuario.getEmail(),
                usuario.getGroupId()
        );
    }

    public void registerUser(RegisterRequest registerRequest) {
        String emailNormalizado = registerRequest.getEmail().trim().toLowerCase();
        if (usuarioRepository.findByEmail(emailNormalizado).isPresent()) {
            throw new RuntimeException("E-mail já está em uso.");
        }

        Usuario usuario = new Usuario();
        usuario.setEmail(emailNormalizado);
        usuario.setSenha(passwordEncoder.encode(registerRequest.getPassword()));
        usuario.setNome(registerRequest.getNome());
        usuario.setSobrenome(registerRequest.getSobrenome());
        usuario.setUnidade(registerRequest.getUnidade());

        // Verifica se o e-mail já foi pré-cadastrado em algum grupo por um Gestor
        var grupoOpt = grupoRepository.findByMembrosEmail(emailNormalizado);
        if (grupoOpt.isPresent()) {
            var grupo = grupoOpt.get();
            usuario.setGroupId(grupo.getHashId());
            
            // Localiza a role delegada pelo Gestor
            String roleDelegada = "OPERADOR";
            if (grupo.getMembros() != null) {
                for (var m : grupo.getMembros()) {
                    if (m.getEmail() != null && m.getEmail().equalsIgnoreCase(emailNormalizado)) {
                        if (m.getRole() != null) {
                            roleDelegada = m.getRole();
                        }
                        break;
                    }
                }
            }
            usuario.setRole(roleDelegada);
            Usuario salvo = usuarioRepository.save(usuario);

            // Atualiza o ID e nome do membro no Grupo
            if (grupo.getMembros() != null) {
                for (var m : grupo.getMembros()) {
                    if (m.getEmail() != null && m.getEmail().equalsIgnoreCase(emailNormalizado)) {
                        m.setUsuarioId(salvo.getId());
                        m.setNome((salvo.getNome() + " " + (salvo.getSobrenome() != null ? salvo.getSobrenome() : "")).trim());
                    }
                }
                grupoRepository.save(grupo);
            }
        } else {
            // Se não pertencer a nenhum grupo ainda, usa a role enviada ou padrão OPERADOR
            String roleInicial = (registerRequest.getRole() != null && !registerRequest.getRole().isBlank()) 
                    ? registerRequest.getRole().toUpperCase() 
                    : "OPERADOR";
            usuario.setRole(roleInicial);
            usuarioRepository.save(usuario);
        }
    }

    public br.com.inovagab.dto.response.UserResponse getCurrentUserProfile(String userId) {
        Usuario usuario = usuarioRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado."));

        return br.com.inovagab.dto.response.UserResponse.builder()
                .id(usuario.getId())
                .nome(usuario.getNome())
                .sobrenome(usuario.getSobrenome())
                .email(usuario.getEmail())
                .role(usuario.getRole() != null ? br.com.inovagab.model.Role.valueOf(usuario.getRole()) : br.com.inovagab.model.Role.OPERADOR)
                .unidade(usuario.getUnidade())
                .ativo(true)
                .groupId(usuario.getGroupId())
                .build();
    }

    private boolean isBCryptHash(String password) {
        if (password == null) return false;
        return password.startsWith("$2a$") || password.startsWith("$2b$") || password.startsWith("$2y$");
    }
}
