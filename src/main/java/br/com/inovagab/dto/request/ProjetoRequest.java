package br.com.inovagab.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ProjetoRequest {

    @NotBlank(message = "Título é obrigatório")
    private String titulo;

    @NotBlank(message = "Área é obrigatória")
    private String area;

    private int etapaAtiva;

    private double progresso;

    private String periodo;

    private double investimento;

    private double investimentoRealizado;

    private double retornoMensalEstimado;

    private double retornoObtido;

    private double aumentoProdutividade;

    private int prazoMeses;

    private String estrategiaId;
}
