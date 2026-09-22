package br.com.inovagab.dto.response;

import br.com.inovagab.model.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private String id;
    private String nome;
    private String sobrenome;
    private String email;
    private Role role;
    private String unidade;
    private boolean ativo;
    private Instant dataCriacao;
    private String groupId;
}
