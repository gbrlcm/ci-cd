package br.com.inovagab.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MembroRequest {
    @NotBlank(message = "O e-mail do membro é obrigatório.")
    @Email(message = "Formato de e-mail inválido.")
    private String email;

    @NotBlank(message = "A ROLE delegada é obrigatória (OPERADOR, LIDER, GESTOR).")
    private String role; // OPERADOR, LIDER, GESTOR
}
