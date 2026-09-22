package br.com.inovagab.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IdeiaResponse {

    private String id;
    private String userId;
    private String autor;
    private String titulo;
    private String descricao;
    private String area;
    private String impacto;
    private String objetivo;
    private String prioridade;
    private String status;
    private String etapa;
    private double progresso;
    private int votos;
    private String estrategiaId;
    private String justificativa;
    private Instant dataCriacao;
    private Instant dataAtualizacao;
}
