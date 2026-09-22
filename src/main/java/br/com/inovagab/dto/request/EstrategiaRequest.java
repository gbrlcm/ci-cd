package br.com.inovagab.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class EstrategiaRequest {

    @NotBlank(message = "Título é obrigatório")
    private String titulo;

    @NotBlank(message = "Descrição é obrigatória")
    private String descricao;

    private String categoria;

    private String campanha;

    /** Planejamento | Em andamento | Concluído */
    private String etapa;

    private double progresso;
}
