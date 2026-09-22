package br.com.inovagab.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResumoResponse {

    private double roiTotalPercentual;
    private double lucroObtidoTotal;
    private double investimentoTotal;
    private int projetosAtivos;
    private int projetosNoPrazo;
    private long ideiasRegistradas;
    private double taxaEngajamento;
    private double aumentoMedioProdutividade;
    private List<RetornoPorEstrategia> retornosPorEstrategia;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RetornoPorEstrategia {
        private String estrategiaId;
        private String estrategiaTitulo;
        private int totalProjetos;
        private double investimentoTotal;
        private double retornoTotal;
        private double roi;
    }
}
