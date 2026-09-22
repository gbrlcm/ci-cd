package br.com.inovagab.controller;

import br.com.inovagab.model.Estrategia;
import br.com.inovagab.model.Ideia;
import br.com.inovagab.model.Projeto;
import br.com.inovagab.model.Comentario;
import br.com.inovagab.model.TransacaoFinanceira;
import br.com.inovagab.service.InovacaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inovacao")
public class InovacaoController {

    @Autowired
    private InovacaoService inovacaoService;

    @Autowired
    private br.com.inovagab.security.SecurityUtils securityUtils;

    // Projetos
    @GetMapping("/projetos")
    public ResponseEntity<List<Projeto>> getAllProjetos() {
        String groupId = securityUtils.getCurrentUserGroupId().orElse(null);
        return ResponseEntity.ok(inovacaoService.getAllProjetos(groupId));
    }

    @PostMapping("/projetos")
    public ResponseEntity<Projeto> addProjeto(@RequestBody Projeto projeto) {
        if (projeto.getId() != null && projeto.getId().isEmpty()) projeto.setId(null);
        String groupId = securityUtils.getCurrentUserGroupId().orElse(null);
        return ResponseEntity.ok(inovacaoService.addProjeto(projeto, groupId));
    }

    @PutMapping("/projetos/{id}")
    public ResponseEntity<?> updateProjeto(@PathVariable String id, @RequestBody Projeto projeto) {
        try {
            String groupId = securityUtils.getCurrentUserGroupId().orElse(null);
            return ResponseEntity.ok(inovacaoService.updateProjeto(id, projeto, groupId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(java.util.Collections.singletonMap("error", e.getMessage()));
        }
    }

    @DeleteMapping("/projetos/{id}")
    public ResponseEntity<?> deleteProjeto(@PathVariable String id) {
        try {
            String groupId = securityUtils.getCurrentUserGroupId().orElse(null);
            inovacaoService.deleteProjeto(id, groupId);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(java.util.Collections.singletonMap("error", e.getMessage()));
        }
    }

    // Estrategias
    @GetMapping("/estrategias")
    public ResponseEntity<List<Estrategia>> getAllEstrategias() {
        String groupId = securityUtils.getCurrentUserGroupId().orElse(null);
        return ResponseEntity.ok(inovacaoService.getAllEstrategias(groupId));
    }

    @PostMapping("/estrategias")
    public ResponseEntity<Estrategia> addEstrategia(@RequestBody Estrategia estrategia) {
        if (estrategia.getId() != null && estrategia.getId().isEmpty()) estrategia.setId(null);
        String groupId = securityUtils.getCurrentUserGroupId().orElse(null);
        return ResponseEntity.ok(inovacaoService.addEstrategia(estrategia, groupId));
    }

    @PutMapping("/estrategias/{id}")
    public ResponseEntity<?> updateEstrategia(@PathVariable String id, @RequestBody Estrategia estrategia) {
        try {
            String groupId = securityUtils.getCurrentUserGroupId().orElse(null);
            return ResponseEntity.ok(inovacaoService.updateEstrategia(id, estrategia, groupId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(java.util.Collections.singletonMap("error", e.getMessage()));
        }
    }

    @DeleteMapping("/estrategias/{id}")
    public ResponseEntity<?> deleteEstrategia(@PathVariable String id) {
        try {
            String groupId = securityUtils.getCurrentUserGroupId().orElse(null);
            inovacaoService.deleteEstrategia(id, groupId);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(java.util.Collections.singletonMap("error", e.getMessage()));
        }
    }

    // Ideias
    @GetMapping("/ideias")
    public ResponseEntity<List<Ideia>> getAllIdeias() {
        String groupId = securityUtils.getCurrentUserGroupId().orElse(null);
        String userId = securityUtils.getCurrentUserId().orElse(null);
        boolean isGestor = securityUtils.isGestor();
        return ResponseEntity.ok(inovacaoService.getAllIdeias(groupId, userId, isGestor));
    }

    @PostMapping("/ideias")
    public ResponseEntity<Ideia> addIdeia(@RequestBody Ideia ideia) {
        if (ideia.getId() != null && ideia.getId().isEmpty()) ideia.setId(null);
        String groupId = securityUtils.getCurrentUserGroupId().orElse(null);
        String userId = securityUtils.getCurrentUserId().orElse(null);
        String userName = securityUtils.getCurrentUser()
                .map(u -> (u.getNome() != null ? u.getNome() : "") + (u.getSobrenome() != null ? " " + u.getSobrenome() : ""))
                .orElse(null);
        return ResponseEntity.ok(inovacaoService.addIdeia(ideia, groupId, userId, userName));
    }

    @PutMapping("/ideias/{id}")
    public ResponseEntity<Ideia> updateIdeia(@PathVariable String id, @RequestBody Ideia ideia) {
        return ResponseEntity.ok(inovacaoService.updateIdeia(id, ideia));
    }

    @DeleteMapping("/ideias/{id}")
    public ResponseEntity<Void> deleteIdeia(@PathVariable String id) {
        inovacaoService.deleteIdeia(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/ideias/{id}/votar")
    public ResponseEntity<Ideia> votarIdeia(@PathVariable String id) {
        return ResponseEntity.ok(inovacaoService.votarIdeia(id));
    }

    @PostMapping("/ideias/{id}/comentar")
    public ResponseEntity<Ideia> comentarIdeia(@PathVariable String id, @RequestBody Comentario comentario) {
        return ResponseEntity.ok(inovacaoService.comentarIdeia(id, comentario));
    }

    // Transacoes Financeiras (Receitas e Despesas)
    @GetMapping("/transacoes")
    public ResponseEntity<List<TransacaoFinanceira>> getTransacoes(
            @RequestParam(required = false) String projetoId,
            @RequestParam(required = false) String tipo) {
        return ResponseEntity.ok(inovacaoService.getTransacoes(projetoId, tipo));
    }

    @PostMapping("/transacoes")
    public ResponseEntity<TransacaoFinanceira> addTransacao(@RequestBody TransacaoFinanceira transacao) {
        return ResponseEntity.ok(inovacaoService.addTransacao(transacao));
    }

    @GetMapping("/receitas")
    public ResponseEntity<List<TransacaoFinanceira>> getReceitas(
            @RequestParam(required = false) String projetoId) {
        return ResponseEntity.ok(inovacaoService.getReceitas(projetoId));
    }

    @PostMapping("/receitas")
    public ResponseEntity<TransacaoFinanceira> addReceita(@RequestBody TransacaoFinanceira receita) {
        return ResponseEntity.ok(inovacaoService.addReceita(receita));
    }

    @GetMapping("/despesas")
    public ResponseEntity<List<TransacaoFinanceira>> getDespesas(
            @RequestParam(required = false) String projetoId) {
        return ResponseEntity.ok(inovacaoService.getDespesas(projetoId));
    }

    @PostMapping("/despesas")
    public ResponseEntity<TransacaoFinanceira> addDespesa(@RequestBody TransacaoFinanceira despesa) {
        return ResponseEntity.ok(inovacaoService.addDespesa(despesa));
    }

    @DeleteMapping("/transacoes/{id}")
    public ResponseEntity<Void> deleteTransacao(@PathVariable String id) {
        inovacaoService.deleteTransacao(id);
        return ResponseEntity.ok().build();
    }

    // Dashboard
    @GetMapping("/dashboard")
    public ResponseEntity<br.com.inovagab.dto.response.DashboardResumoResponse> getDashboardResumo() {
        String groupId = securityUtils.getCurrentUserGroupId().orElse(null);
        return ResponseEntity.ok(inovacaoService.getDashboardResumo(groupId));
    }
}
