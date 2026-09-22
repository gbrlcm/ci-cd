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
public class ProjetoResponse {

    private String id;
    private String titulo;
    private String area;
    private String status;
    private int etapaAtiva;
    private double progresso;
    private String periodo;
    private double investimento;
    private double investimentoRealizado;
    private double retornoMensalEstimado;
    private double retornoObtido;
    private double lucroObtido;
    private double roiPercentual;
    private double aumentoProdutividadePercentual;
    private int prazoMeses;
    private String estrategiaId;
    private Instant dataInicio;
    private Instant dataAtualizacao;
}
