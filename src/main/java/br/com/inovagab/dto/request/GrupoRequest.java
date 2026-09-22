package br.com.inovagab.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GrupoRequest {

    @NotBlank(message = "O nome do grupo é obrigatório.")
    private String nome;

    @NotBlank(message = "O departamento é obrigatório.")
    private String departamento;

    private String descricao;

    private List<MembroRequest> membros = new ArrayList<>();
}
