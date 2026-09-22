package br.com.inovagab.repository;

import br.com.inovagab.model.Grupo;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GrupoRepository extends MongoRepository<Grupo, String> {
    
    Optional<Grupo> findByHashId(String hashId);
    
    List<Grupo> findByGestorId(String gestorId);
    
    List<Grupo> findByDepartamentoIgnoreCase(String departamento);
    
    @Query("{ 'membros.email': ?0 }")
    Optional<Grupo> findByMembrosEmail(String email);
    
    boolean existsByHashId(String hashId);
}
