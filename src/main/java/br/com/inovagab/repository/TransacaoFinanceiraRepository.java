package br.com.inovagab.repository;

import br.com.inovagab.model.TransacaoFinanceira;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransacaoFinanceiraRepository extends MongoRepository<TransacaoFinanceira, String> {
    List<TransacaoFinanceira> findAllByOrderByDataHoraDesc();
    List<TransacaoFinanceira> findByGroupIdOrderByDataHoraDesc(String groupId);
    List<TransacaoFinanceira> findByProjetoIdOrderByDataHoraDesc(String projetoId);
    List<TransacaoFinanceira> findByTipoOrderByDataHoraDesc(String tipo);
    List<TransacaoFinanceira> findByProjetoIdAndTipoOrderByDataHoraDesc(String projetoId, String tipo);
}
