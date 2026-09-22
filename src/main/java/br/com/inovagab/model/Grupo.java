package br.com.inovagab.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "grupos")
public class Grupo {

    @Id
    private String id;
    
    private String hashId; // Identificador único do grupo em hash (GroupId)
    
    private String nome;
    
    private String departamento;
    
    private String descricao;
    
    private String gestorId;
    
    private String gestorEmail;
    
    @Builder.Default
    private LocalDateTime dataCriacao = LocalDateTime.now();
    
    @Builder.Default
    private List<MembroGrupo> membros = new ArrayList<>();
}
