package br.com.inovagab.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "estrategias")
public class Estrategia {
    @Id
    private String id;
    private String titulo;
    private String descricao;
    private String status;
    private Integer statusColor;
    private Integer statusTextColor;
    private Double progresso;
    private String dataCriacao;
    private String categoria;
    private String campanha;
    private String dataVigencia;
    private String orientacoes;
    private String groupId;
}

