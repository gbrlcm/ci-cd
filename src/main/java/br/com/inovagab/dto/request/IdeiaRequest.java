package br.com.inovagab.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class IdeiaRequest {

    @NotBlank(message = "Título é obrigatório")
    private String titulo;

    @NotBlank(message = "Descrição é obrigatória")
    private String descricao;

    @NotBlank(message = "Área é obrigatória")
    private String area;

    /** Alto | Médio | Baixo */
    private String impacto;

    private String objetivo;

    /** Alta | Média | Baixa */
    private String prioridade;

    private String estrategiaId;
}
