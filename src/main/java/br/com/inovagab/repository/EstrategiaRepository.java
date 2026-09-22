package br.com.inovagab.repository;

import br.com.inovagab.model.Estrategia;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface EstrategiaRepository extends MongoRepository<Estrategia, String> {
    List<Estrategia> findByGroupId(String groupId);
}
