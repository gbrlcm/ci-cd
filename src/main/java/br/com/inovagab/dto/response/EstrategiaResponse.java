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
public class EstrategiaResponse {

    private String id;
    private String titulo;
    private String descricao;
    private String categoria;
    private String campanha;
    private String etapa;
    private double progresso;
    private boolean ativa;
    private Instant dataCriacao;
    private Instant dataAtualizacao;
}
