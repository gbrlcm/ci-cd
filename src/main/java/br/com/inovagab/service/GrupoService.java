package br.com.inovagab.service;

import br.com.inovagab.dto.request.GrupoRequest;
import br.com.inovagab.dto.request.MembroRequest;
import br.com.inovagab.dto.response.GrupoResponse;
import br.com.inovagab.dto.response.UserResponse;
import br.com.inovagab.model.Grupo;
import br.com.inovagab.model.MembroGrupo;
import br.com.inovagab.model.Role;
import br.com.inovagab.model.Usuario;
import br.com.inovagab.repository.GrupoRepository;
import br.com.inovagab.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class GrupoService {

    private final GrupoRepository grupoRepository;
    private final UsuarioRepository usuarioRepository;

    @Autowired
    public GrupoService(GrupoRepository grupoRepository, UsuarioRepository usuarioRepository) {
        this.grupoRepository = grupoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public GrupoResponse criarGrupo(GrupoRequest request, String gestorId) {
        Usuario gestor = usuarioRepository.findById(gestorId)
                .orElseThrow(() -> new RuntimeException("Gestor não encontrado no sistema."));

        if (!"GESTOR".equalsIgnoreCase(gestor.getRole())) {
            throw new RuntimeException("Apenas usuários com perfil GESTOR podem criar grupos.");
        }

        String hashId = generateUniqueHash();

        List<MembroGrupo> membros = new ArrayList<>();

        // Adiciona o próprio gestor como primeiro membro com role GESTOR
        String nomeCompletoGestor = (gestor.getNome() != null ? gestor.getNome() : "")
                + (gestor.getSobrenome() != null ? " " + gestor.getSobrenome() : "");

        membros.add(MembroGrupo.builder()
                .usuarioId(gestor.getId())
                .email(gestor.getEmail())
                .nome(nomeCompletoGestor.trim())
                .role("GESTOR")
                .dataAdicao(LocalDateTime.now())
                .build());

        // Atualiza o groupId do próprio gestor
        gestor.setGroupId(hashId);
        usuarioRepository.save(gestor);

        // Processa os membros delegados pelo gestor
        if (request.getMembros() != null) {
            for (MembroRequest membroReq : request.getMembros()) {
                if (membroReq.getEmail() == null || membroReq.getEmail().isBlank()) continue;
                String emailNormalizado = membroReq.getEmail().trim().toLowerCase();

                // Ignora se for o próprio gestor já adicionado
                if (emailNormalizado.equalsIgnoreCase(gestor.getEmail())) continue;

                String roleDelegada = validarRole(membroReq.getRole());

                Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(emailNormalizado);
                if (usuarioOpt.isPresent()) {
                    Usuario u = usuarioOpt.get();
                    u.setRole(roleDelegada);
                    u.setGroupId(hashId);
                    usuarioRepository.save(u);

                    String nomeCompleto = (u.getNome() != null ? u.getNome() : "")
                            + (u.getSobrenome() != null ? " " + u.getSobrenome() : "");

                    membros.add(MembroGrupo.builder()
                            .usuarioId(u.getId())
                            .email(u.getEmail())
                            .nome(nomeCompleto.trim())
                            .role(roleDelegada)
                            .dataAdicao(LocalDateTime.now())
                            .build());
                } else {
                    // Usuário ainda não cadastrado no app: registra pendente no grupo
                    membros.add(MembroGrupo.builder()
                            .usuarioId(null)
                            .email(emailNormalizado)
                            .nome("Pendente de Cadastro")
                            .role(roleDelegada)
                            .dataAdicao(LocalDateTime.now())
                            .build());
                }
            }
        }

        Grupo grupo = Grupo.builder()
                .hashId(hashId)
                .nome(request.getNome())
                .departamento(request.getDepartamento())
                .descricao(request.getDescricao())
                .gestorId(gestor.getId())
                .gestorEmail(gestor.getEmail())
                .dataCriacao(LocalDateTime.now())
                .membros(membros)
                .build();

        Grupo salvo = grupoRepository.save(grupo);
        return toResponse(salvo);
    }

    public GrupoResponse adicionarOuAtualizarMembro(String hashId, MembroRequest request, String gestorId) {
        Grupo grupo = grupoRepository.findByHashId(hashId)
                .orElseThrow(() -> new RuntimeException("Grupo não encontrado para o hash: " + hashId));

        validarGestorDoGrupo(grupo, gestorId);

        String emailNormalizado = request.getEmail().trim().toLowerCase();
        String roleDelegada = validarRole(request.getRole());

        // Remove membro anterior com mesmo e-mail na lista interna, se houver
        grupo.getMembros().removeIf(m -> m.getEmail().equalsIgnoreCase(emailNormalizado));

        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(emailNormalizado);
        if (usuarioOpt.isPresent()) {
            Usuario u = usuarioOpt.get();
            u.setRole(roleDelegada);
            u.setGroupId(hashId);
            usuarioRepository.save(u);

            String nomeCompleto = (u.getNome() != null ? u.getNome() : "")
                    + (u.getSobrenome() != null ? " " + u.getSobrenome() : "");

            grupo.getMembros().add(MembroGrupo.builder()
                    .usuarioId(u.getId())
                    .email(u.getEmail())
                    .nome(nomeCompleto.trim())
                    .role(roleDelegada)
                    .dataAdicao(LocalDateTime.now())
                    .build());
        } else {
            grupo.getMembros().add(MembroGrupo.builder()
                    .usuarioId(null)
                    .email(emailNormalizado)
                    .nome("Pendente de Cadastro")
                    .role(roleDelegada)
                    .dataAdicao(LocalDateTime.now())
                    .build());
        }

        Grupo atualizado = grupoRepository.save(grupo);
        return toResponse(atualizado);
    }

    public GrupoResponse removerMembro(String hashId, String email, String gestorId) {
        Grupo grupo = grupoRepository.findByHashId(hashId)
                .orElseThrow(() -> new RuntimeException("Grupo não encontrado para o hash: " + hashId));

        validarGestorDoGrupo(grupo, gestorId);

        String emailNormalizado = email.trim().toLowerCase();

        // Não permite remover o gestor criador
        if (emailNormalizado.equalsIgnoreCase(grupo.getGestorEmail())) {
            throw new RuntimeException("Não é permitido remover o Gestor principal do grupo.");
        }

        boolean removido = grupo.getMembros().removeIf(m -> m.getEmail().equalsIgnoreCase(emailNormalizado));
        if (!removido) {
            throw new RuntimeException("Membro com e-mail " + email + " não pertence a este grupo.");
        }

        usuarioRepository.findByEmail(emailNormalizado).ifPresent(u -> {
            if (hashId.equals(u.getGroupId())) {
                u.setGroupId(null);
                u.setRole("OPERADOR"); // Retorna para a role padrão de colaborador
                usuarioRepository.save(u);
            }
        });

        Grupo atualizado = grupoRepository.save(grupo);
        return toResponse(atualizado);
    }

    public List<GrupoResponse> listarGruposDoGestor(String gestorId) {
        Usuario gestor = usuarioRepository.findById(gestorId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado."));

        if (!"GESTOR".equalsIgnoreCase(gestor.getRole())) {
            throw new RuntimeException("Acesso negado: apenas Gestores podem consultar seus grupos.");
        }

        List<Grupo> grupos = grupoRepository.findByGestorId(gestorId);
        return grupos.stream().map(this::toResponse).collect(Collectors.toList());
    }

    public GrupoResponse obterGrupoPorHash(String hashId, String usuarioId) {
        Grupo grupo = grupoRepository.findByHashId(hashId)
                .orElseThrow(() -> new RuntimeException("Grupo não encontrado para o hash: " + hashId));

        // Proteção contra IDOR: apenas o gestor do grupo ou membros vinculados podem visualizar os dados
        boolean isGestor = usuarioId != null && (usuarioId.equals(grupo.getGestorId()));
        boolean isMembro = usuarioId != null && grupo.getMembros() != null && grupo.getMembros().stream()
                .anyMatch(m -> usuarioId.equals(m.getUsuarioId()));

        if (!isGestor && !isMembro) {
            throw new RuntimeException("Acesso negado: você não tem permissão para visualizar os detalhes deste grupo.");
        }

        return toResponse(grupo);
    }

    public GrupoResponse atualizarGrupo(String hashId, GrupoRequest request, String gestorId) {
        Grupo grupo = grupoRepository.findByHashId(hashId)
                .orElseThrow(() -> new RuntimeException("Grupo não encontrado para o hash: " + hashId));

        validarGestorDoGrupo(grupo, gestorId);

        if (request.getNome() != null && !request.getNome().isBlank()) {
            grupo.setNome(request.getNome());
        }
        if (request.getDepartamento() != null && !request.getDepartamento().isBlank()) {
            grupo.setDepartamento(request.getDepartamento());
        }
        if (request.getDescricao() != null) {
            grupo.setDescricao(request.getDescricao());
        }

        Grupo atualizado = grupoRepository.save(grupo);
        return toResponse(atualizado);
    }

    public List<UserResponse> listarUsuariosDisponiveis(String busca) {
        List<Usuario> usuarios;
        if (busca != null && !busca.isBlank()) {
            usuarios = usuarioRepository.findByNomeContainingIgnoreCaseOrSobrenomeContainingIgnoreCaseOrEmailContainingIgnoreCase(busca, busca, busca);
        } else {
            usuarios = usuarioRepository.findAll();
        }

        return usuarios.stream().map(u -> UserResponse.builder()
                .id(u.getId())
                .nome(u.getNome())
                .sobrenome(u.getSobrenome())
                .email(u.getEmail())
                .role(u.getRole() != null ? Role.valueOf(u.getRole()) : Role.OPERADOR)
                .unidade(u.getUnidade())
                .ativo(true)
                .groupId(u.getGroupId())
                .build()
        ).collect(Collectors.toList());
    }

    private String validarRole(String role) {
        if (role == null || role.isBlank()) {
            return "OPERADOR";
        }
        String upper = role.trim().toUpperCase();
        try {
            Role.valueOf(upper);
            return upper;
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Role inválida: " + role + ". Valores permitidos: OPERADOR, LIDER, GESTOR.");
        }
    }

    private void validarGestorDoGrupo(Grupo grupo, String gestorId) {
        Usuario usuario = usuarioRepository.findById(gestorId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado."));

        if (!"GESTOR".equalsIgnoreCase(usuario.getRole())) {
            throw new RuntimeException("Acesso negado: apenas Gestores podem alterar membros de grupos.");
        }

        if (!gestorId.equals(grupo.getGestorId()) && !usuario.getEmail().equalsIgnoreCase(grupo.getGestorEmail())) {
            throw new RuntimeException("Acesso negado: você não é o gestor responsável por este grupo.");
        }
    }

    private String generateUniqueHash() {
        String hash;
        do {
            hash = "GRP-" + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        } while (grupoRepository.existsByHashId(hash));
        return hash;
    }

    private GrupoResponse toResponse(Grupo g) {
        return GrupoResponse.builder()
                .id(g.getId())
                .hashId(g.getHashId())
                .nome(g.getNome())
                .departamento(g.getDepartamento())
                .descricao(g.getDescricao())
                .gestorId(g.getGestorId())
                .gestorEmail(g.getGestorEmail())
                .dataCriacao(g.getDataCriacao())
                .membros(g.getMembros())
                .totalMembros(g.getMembros() != null ? g.getMembros().size() : 0)
                .build();
    }
}
