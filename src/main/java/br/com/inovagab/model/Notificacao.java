package br.com.inovagab.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "notificacoes")
public class Notificacao {
    @Id
    private String id;
    private String mensagem;
    private boolean lida;
    private LocalDateTime dataCriacao;
    private String destinatarioRole; // GESTOR, LIDER, etc.
    private String ideiaId;
    private String tipo; // NOVA_IDEIA, ATUALIZACAO
}