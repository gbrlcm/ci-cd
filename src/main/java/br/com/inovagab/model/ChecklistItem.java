package br.com.inovagab.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChecklistItem {
    private String id;
    private String titulo;
    private Boolean concluido;
}
