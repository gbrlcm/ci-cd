package br.com.inovagab.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class IdeiaStatusRequest {

    @NotBlank(message = "Status é obrigatório")
    private String status;

    private String justificativa;
}
