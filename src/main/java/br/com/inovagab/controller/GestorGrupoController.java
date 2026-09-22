package br.com.inovagab.controller;

import br.com.inovagab.dto.request.GrupoRequest;
import br.com.inovagab.dto.request.MembroRequest;
import br.com.inovagab.dto.response.GrupoResponse;
import br.com.inovagab.dto.response.UserResponse;
import br.com.inovagab.model.Usuario;
import br.com.inovagab.security.SecurityUtils;
import br.com.inovagab.service.GrupoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/gestor")
public class GestorGrupoController {

    private final GrupoService grupoService;
    private final SecurityUtils securityUtils;

    @Autowired
    public GestorGrupoController(GrupoService grupoService, SecurityUtils securityUtils) {
        this.grupoService = grupoService;
        this.securityUtils = securityUtils;
    }

    private String requireGestorId() {
        Usuario usuario = securityUtils.getCurrentUser()
                .orElseThrow(() -> new RuntimeException("Usuário não autenticado."));
        if (!"GESTOR".equalsIgnoreCase(usuario.getRole())) {
            throw new RuntimeException("Acesso negado: este endpoint é exclusivo para usuários com perfil GESTOR.");
        }
        return usuario.getId();
    }

    @PostMapping("/grupos")
    public ResponseEntity<?> criarGrupo(@Valid @RequestBody GrupoRequest request) {
        try {
            String gestorId = requireGestorId();
            GrupoResponse response = grupoService.criarGrupo(request, gestorId);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Collections.singletonMap("error", e.getMessage()));
        }
    }

    @GetMapping("/grupos")
    public ResponseEntity<?> listarGrupos() {
        try {
            String gestorId = requireGestorId();
            List<GrupoResponse> grupos = grupoService.listarGruposDoGestor(gestorId);
            return ResponseEntity.ok(grupos);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Collections.singletonMap("error", e.getMessage()));
        }
    }

    @GetMapping("/grupos/{groupId}")
    public ResponseEntity<?> obterGrupo(@PathVariable String groupId) {
        try {
            String usuarioId = securityUtils.getCurrentUserId()
                    .orElseThrow(() -> new RuntimeException("Usuário não autenticado."));

            GrupoResponse response = grupoService.obterGrupoPorHash(groupId, usuarioId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Collections.singletonMap("error", e.getMessage()));
        }
    }

    @PutMapping("/grupos/{groupId}")
    public ResponseEntity<?> atualizarGrupo(@PathVariable String groupId, @Valid @RequestBody GrupoRequest request) {
        try {
            String gestorId = requireGestorId();
            GrupoResponse response = grupoService.atualizarGrupo(groupId, request, gestorId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Collections.singletonMap("error", e.getMessage()));
        }
    }

    @PostMapping("/grupos/{groupId}/membros")
    public ResponseEntity<?> adicionarOuAtualizarMembro(@PathVariable String groupId, @Valid @RequestBody MembroRequest request) {
        try {
            String gestorId = requireGestorId();
            GrupoResponse response = grupoService.adicionarOuAtualizarMembro(groupId, request, gestorId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Collections.singletonMap("error", e.getMessage()));
        }
    }

    @DeleteMapping("/grupos/{groupId}/membros/{email}")
    public ResponseEntity<?> removerMembro(@PathVariable String groupId, @PathVariable String email) {
        try {
            String gestorId = requireGestorId();
            GrupoResponse response = grupoService.removerMembro(groupId, email, gestorId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Collections.singletonMap("error", e.getMessage()));
        }
    }

    @GetMapping("/usuarios")
    public ResponseEntity<?> listarUsuariosDisponiveis(@RequestParam(required = false) String busca) {
        try {
            requireGestorId();
            List<UserResponse> usuarios = grupoService.listarUsuariosDisponiveis(busca);
            return ResponseEntity.ok(usuarios);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Collections.singletonMap("error", e.getMessage()));
        }
    }
}
