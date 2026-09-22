package br.com.inovagab.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HistoricoEstrategiaResponse {

    private String id;
    private String estrategiaId;
    private Instant dataRegistro;
    private String categoria;
    private String campanha;
    private String resultadoFinal;
    private double roiAlcancado;
}
