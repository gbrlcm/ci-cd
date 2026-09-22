package br.com.inovagab.dto.response;

import br.com.inovagab.model.MembroGrupo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GrupoResponse {
    private String id;
    private String hashId; // groupId
    private String nome;
    private String departamento;
    private String descricao;
    private String gestorId;
    private String gestorEmail;
    private LocalDateTime dataCriacao;
    private List<MembroGrupo> membros;
    private int totalMembros;
}
