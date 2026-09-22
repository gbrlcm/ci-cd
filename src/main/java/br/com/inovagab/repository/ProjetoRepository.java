package br.com.inovagab.repository;

import br.com.inovagab.model.Projeto;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ProjetoRepository extends MongoRepository<Projeto, String> {
    List<Projeto> findByGroupId(String groupId);
}
