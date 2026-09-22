package br.com.inovagab.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "historico_estrategias")
public class HistoricoEstrategia {

    @Id
    private String id;

    private String estrategiaId;

    private Instant dataRegistro;

    private String categoria;

    private String campanha;

    private String resultadoFinal;

    @Builder.Default
    private double roiAlcancado = 0.0;
}
