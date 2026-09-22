package br.com.inovagab.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "transacoes_financeiras")
public class TransacaoFinanceira {

    @Id
    private String id;

    private String projetoId;

    private String projetoTitulo;

    /**
     * Tipo da transação: "RECEITA" ou "DESPESA"
     */
    private String tipo;

    private String descricao;

    private Double valor;

    private String categoria;

    private String dataHora;

    private String responsavel;
    private String groupId;
}
