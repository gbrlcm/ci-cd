package br.com.inovagab.repository;

import br.com.inovagab.model.HistoricoEstrategia;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface HistoricoEstrategiaRepository extends MongoRepository<HistoricoEstrategia, String> {

    List<HistoricoEstrategia> findByEstrategiaIdOrderByDataRegistroDesc(String estrategiaId);
}
