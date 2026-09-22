package br.com.inovagab.repository;

import br.com.inovagab.model.Ideia;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface IdeiaRepository extends MongoRepository<Ideia, String> {
    List<Ideia> findByGroupId(String groupId);
    List<Ideia> findByUserId(String userId);
    List<Ideia> findByGroupIdIsNull();
    long countByGroupId(String groupId);
}
