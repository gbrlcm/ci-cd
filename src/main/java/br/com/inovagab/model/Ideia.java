package br.com.inovagab.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;
import java.util.ArrayList;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "ideias")
public class Ideia {
    @Id
    private String id;
    private String titulo;
    private String descricao;
    private String autor;
    private String area;
    private String status;
    private Integer statusColor;
    private Integer statusTextColor;
    private String tempo;
    private Double progresso;
    private String etapa;
    private String destaque;
    private String userId;
    private Integer votos;
    private String impacto;
    private String objetivo;
    private String prioridade;
    private Integer prioridadeColor;
    private Integer prioridadeBg;
    private String estrategiaId;
    private String estrategiaTitulo;
    
    private List<Comentario> comentarios = new ArrayList<>();
    private String groupId;
}
