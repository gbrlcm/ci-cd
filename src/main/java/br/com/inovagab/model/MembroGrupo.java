package br.com.inovagab.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MembroGrupo {
    private String usuarioId;
    private String email;
    private String nome;
    private String role; // GESTOR, LIDER, OPERADOR
    private LocalDateTime dataAdicao;
}
