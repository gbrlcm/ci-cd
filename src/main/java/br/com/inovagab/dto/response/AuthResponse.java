package br.com.inovagab.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import lombok.Builder;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {
    private String token;
    private String userId;
    private String role;
    private String nome;
    private String sobrenome;
    private String unidade;
    private String email;
    private String groupId;

    public AuthResponse(String token, String userId, String role, String nome, String sobrenome, String unidade, String email) {
        this.token = token;
        this.userId = userId;
        this.role = role;
        this.nome = nome;
        this.sobrenome = sobrenome;
        this.unidade = unidade;
        this.email = email;
    }
}
