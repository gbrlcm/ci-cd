package br.com.inovagab.controller;

import br.com.inovagab.model.Notificacao;
import br.com.inovagab.repository.NotificacaoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notificacoes")
@CrossOrigin(origins = "*")
public class NotificacaoController {

    @Autowired
    private NotificacaoRepository notificacaoRepository;

    @GetMapping("/{role}")
    public List<Notificacao> getNotificacoes(@PathVariable String role) {
        return notificacaoRepository.findByDestinatarioRole(role.toUpperCase());
    }

    @PutMapping("/{id}/lida")
    public Notificacao marcarComoLida(@PathVariable String id) {
        Notificacao notificacao = notificacaoRepository.findById(id).orElseThrow(() -> new RuntimeException("Notificacao nao encontrada"));
        notificacao.setLida(true);
        return notificacaoRepository.save(notificacao);
    }
}