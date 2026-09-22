package br.com.inovagab.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Comentario {
    private String autor;
    private String texto;
    private LocalDateTime dataHora = LocalDateTime.now();
}
