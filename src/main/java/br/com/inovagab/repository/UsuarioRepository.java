package br.com.inovagab.repository;

import br.com.inovagab.model.Usuario;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends MongoRepository<Usuario, String> {
    Optional<Usuario> findByEmail(String email);
    List<Usuario> findByGroupId(String groupId);
    List<Usuario> findByNomeContainingIgnoreCaseOrSobrenomeContainingIgnoreCaseOrEmailContainingIgnoreCase(String nome, String sobrenome, String email);
}
