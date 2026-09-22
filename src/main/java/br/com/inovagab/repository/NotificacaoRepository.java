package br.com.inovagab.repository;

import br.com.inovagab.model.Notificacao;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface NotificacaoRepository extends MongoRepository<Notificacao, String> {
    List<Notificacao> findByDestinatarioRole(String destinatarioRole);
}