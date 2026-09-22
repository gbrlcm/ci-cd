package br.com.inovagab.security;

import br.com.inovagab.model.Usuario;
import br.com.inovagab.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class SecurityUtils {

    private final UsuarioRepository usuarioRepository;

    @Autowired
    public SecurityUtils(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public Optional<String> getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && authentication.getPrincipal() != null) {
            String principal = authentication.getPrincipal().toString();
            if (!"anonymousUser".equalsIgnoreCase(principal)) {
                return Optional.of(principal);
            }
        }
        return Optional.empty();
    }

    public Optional<Usuario> getCurrentUser() {
        return getCurrentUserId().flatMap(usuarioRepository::findById);
    }

    public Optional<String> getCurrentUserGroupId() {
        return getCurrentUser().map(Usuario::getGroupId);
    }

    public boolean isGestor() {
        return getCurrentUser()
                .map(u -> "GESTOR".equalsIgnoreCase(u.getRole()))
                .orElse(false);
    }
}
