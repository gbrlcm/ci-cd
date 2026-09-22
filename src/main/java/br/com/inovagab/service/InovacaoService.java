package br.com.inovagab.service;

import br.com.inovagab.model.Estrategia;
import br.com.inovagab.model.Ideia;
import br.com.inovagab.model.Projeto;
import br.com.inovagab.model.Notificacao;
import br.com.inovagab.model.Comentario;
import br.com.inovagab.model.TransacaoFinanceira;
import br.com.inovagab.repository.EstrategiaRepository;
import br.com.inovagab.repository.IdeiaRepository;
import br.com.inovagab.repository.ProjetoRepository;
import br.com.inovagab.repository.NotificacaoRepository;
import br.com.inovagab.repository.TransacaoFinanceiraRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InovacaoService {

    @Autowired
    private ProjetoRepository projetoRepository;
    
    @Autowired
    private EstrategiaRepository estrategiaRepository;
    
    @Autowired
    private IdeiaRepository ideiaRepository;

    @Autowired
    private NotificacaoRepository notificacaoRepository;

    @Autowired
    private TransacaoFinanceiraRepository transacaoFinanceiraRepository;

    public List<Projeto> getAllProjetos() {
        return getAllProjetos(null);
    }

    public List<Projeto> getAllProjetos(String groupId) {
        if (groupId != null && !groupId.isBlank()) {
            return projetoRepository.findByGroupId(groupId);
        }
        // Proteção contra vazamento: usuário sem grupo não visualiza projetos de outros grupos
        return java.util.Collections.emptyList();
    }

    public Projeto addProjeto(Projeto projeto) {
        return addProjeto(projeto, null);
    }

    public Projeto addProjeto(Projeto projeto, String groupId) {
        if ((projeto.getGroupId() == null || projeto.getGroupId().isBlank()) && groupId != null && !groupId.isBlank()) {
            projeto.setGroupId(groupId);
        }
        return projetoRepository.save(projeto);
    }

    public Projeto updateProjeto(String id, Projeto projetoUpdates) {
        return updateProjeto(id, projetoUpdates, null);
    }

    public Projeto updateProjeto(String id, Projeto projetoUpdates, String groupId) {
        return projetoRepository.findById(id).map(p -> {
            // Proteção contra IDOR: impede que um usuário altere projetos pertencentes a outro grupo
            if (p.getGroupId() != null && groupId != null && !p.getGroupId().equals(groupId)) {
                throw new RuntimeException("Acesso negado: este projeto não pertence ao seu grupo.");
            }

            if(projetoUpdates.getTitulo() != null) p.setTitulo(projetoUpdates.getTitulo());
            if(projetoUpdates.getArea() != null) p.setArea(projetoUpdates.getArea());
            if(projetoUpdates.getProgresso() != null) p.setProgresso(projetoUpdates.getProgresso());
            if(projetoUpdates.getEtapaAtiva() != null) p.setEtapaAtiva(projetoUpdates.getEtapaAtiva());
            if(projetoUpdates.getStatus() != null) p.setStatus(projetoUpdates.getStatus());
            if(projetoUpdates.getProgressoTexto() != null) p.setProgressoTexto(projetoUpdates.getProgressoTexto());
            if(projetoUpdates.getStatusColor() != null) p.setStatusColor(projetoUpdates.getStatusColor());
            if(projetoUpdates.getStatusBg() != null) p.setStatusBg(projetoUpdates.getStatusBg());
            
            if(projetoUpdates.getResultadoValor() != null) p.setResultadoValor(projetoUpdates.getResultadoValor());
            if(projetoUpdates.getResultadoRoi() != null) p.setResultadoRoi(projetoUpdates.getResultadoRoi());
            if(projetoUpdates.getInvestimento() != null) p.setInvestimento(projetoUpdates.getInvestimento());
            if(projetoUpdates.getEstrategiaId() != null) p.setEstrategiaId(projetoUpdates.getEstrategiaId());
            if(projetoUpdates.getEstrategiaTitulo() != null) p.setEstrategiaTitulo(projetoUpdates.getEstrategiaTitulo());
            if(projetoUpdates.getLucroObtido() != null) p.setLucroObtido(projetoUpdates.getLucroObtido());
            if(projetoUpdates.getAumentoProdutividade() != null) p.setAumentoProdutividade(projetoUpdates.getAumentoProdutividade());
            if(projetoUpdates.getNoPrazo() != null) p.setNoPrazo(projetoUpdates.getNoPrazo());
            if(projetoUpdates.getDataInicio() != null) p.setDataInicio(projetoUpdates.getDataInicio());
            if(projetoUpdates.getPrazo() != null) p.setPrazo(projetoUpdates.getPrazo());
            if(projetoUpdates.getTarefas() != null) p.setTarefas(projetoUpdates.getTarefas());

            if(projetoUpdates.getValorMensal() != null) p.setValorMensal(projetoUpdates.getValorMensal());
            if(projetoUpdates.getRoi() != null) p.setRoi(projetoUpdates.getRoi());
            if(projetoUpdates.getDuracao() != null) p.setDuracao(projetoUpdates.getDuracao());
            if(projetoUpdates.getResultadosAlcancados() != null) p.setResultadosAlcancados(projetoUpdates.getResultadosAlcancados());

            return projetoRepository.save(p);
        }).orElseThrow(() -> new RuntimeException("Projeto não encontrado"));
    }

    public void deleteProjeto(String id) {
        deleteProjeto(id, null);
    }

    public void deleteProjeto(String id, String groupId) {
        Projeto p = projetoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Projeto não encontrado"));

        // Proteção contra IDOR: impede exclusão de projeto de outro grupo
        if (p.getGroupId() != null && groupId != null && !p.getGroupId().equals(groupId)) {
            throw new RuntimeException("Acesso negado: este projeto não pertence ao seu grupo.");
        }

        projetoRepository.deleteById(id);
    }

    // Estrategia
    public List<Estrategia> getAllEstrategias() {
        return getAllEstrategias(null);
    }

    public List<Estrategia> getAllEstrategias(String groupId) {
        if (groupId != null && !groupId.isBlank()) {
            return estrategiaRepository.findByGroupId(groupId);
        }
        return java.util.Collections.emptyList();
    }

    public Estrategia addEstrategia(Estrategia estrategia) {
        return addEstrategia(estrategia, null);
    }

    public Estrategia addEstrategia(Estrategia estrategia, String groupId) {
        if ((estrategia.getGroupId() == null || estrategia.getGroupId().isBlank()) && groupId != null && !groupId.isBlank()) {
            estrategia.setGroupId(groupId);
        }
        return estrategiaRepository.save(estrategia);
    }

    public Estrategia updateEstrategia(String id, Estrategia estrategiaUpdates) {
        return updateEstrategia(id, estrategiaUpdates, null);
    }

    public Estrategia updateEstrategia(String id, Estrategia estrategiaUpdates, String groupId) {
        return estrategiaRepository.findById(id).map(e -> {
            // Proteção contra IDOR: impede alterar estratégia de outro grupo
            if (e.getGroupId() != null && groupId != null && !e.getGroupId().equals(groupId)) {
                throw new RuntimeException("Acesso negado: esta estratégia não pertence ao seu grupo.");
            }

            if(estrategiaUpdates.getTitulo() != null) e.setTitulo(estrategiaUpdates.getTitulo());
            if(estrategiaUpdates.getDescricao() != null) e.setDescricao(estrategiaUpdates.getDescricao());
            if(estrategiaUpdates.getProgresso() != null) e.setProgresso(estrategiaUpdates.getProgresso());
            if(estrategiaUpdates.getStatus() != null) e.setStatus(estrategiaUpdates.getStatus());
            if(estrategiaUpdates.getStatusColor() != null) e.setStatusColor(estrategiaUpdates.getStatusColor());
            if(estrategiaUpdates.getStatusTextColor() != null) e.setStatusTextColor(estrategiaUpdates.getStatusTextColor());
            
            if(estrategiaUpdates.getCategoria() != null) e.setCategoria(estrategiaUpdates.getCategoria());
            if(estrategiaUpdates.getCampanha() != null) e.setCampanha(estrategiaUpdates.getCampanha());

            return estrategiaRepository.save(e);
        }).orElseThrow(() -> new RuntimeException("Estratégia não encontrada"));
    }

    public void deleteEstrategia(String id) {
        deleteEstrategia(id, null);
    }

    public void deleteEstrategia(String id, String groupId) {
        Estrategia e = estrategiaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Estratégia não encontrada"));

        // Proteção contra IDOR: impede exclusão de estratégia de outro grupo
        if (e.getGroupId() != null && groupId != null && !e.getGroupId().equals(groupId)) {
            throw new RuntimeException("Acesso negado: esta estratégia não pertence ao seu grupo.");
        }

        estrategiaRepository.deleteById(id);
    }

    // Ideia
    public List<Ideia> getAllIdeias() {
        return getAllIdeias(null, null, true);
    }

    public List<Ideia> getAllIdeias(String groupId) {
        return getAllIdeias(groupId, null, false);
    }

    public List<Ideia> getAllIdeias(String groupId, String userId, boolean isGestor) {
        if (isGestor) {
            if (groupId != null && !groupId.isBlank()) {
                return ideiaRepository.findAll().stream()
                        .filter(i -> groupId.equals(i.getGroupId()) || (userId != null && userId.equals(i.getUserId())))
                        .collect(java.util.stream.Collectors.toList());
            }
            return ideiaRepository.findAll();
        }

        List<Ideia> todas = ideiaRepository.findAll();
        List<Ideia> permitidas = new java.util.ArrayList<>();
        for (Ideia i : todas) {
            boolean isMinha = (userId != null && !userId.isBlank() && userId.equals(i.getUserId()));
            boolean isDoMesmoGrupo = (groupId != null && !groupId.isBlank() && groupId.equals(i.getGroupId()));

            // Ideia pessoal: apenas o autor visualiza.
            // Ideia do grupo: visivel para quem pertence ao mesmo grupo.
            if (isMinha || isDoMesmoGrupo) {
                permitidas.add(i);
            }
        }
        return permitidas;
    }

    public Ideia addIdeia(Ideia ideia) {
        return addIdeia(ideia, null, null, null);
    }

    public Ideia addIdeia(Ideia ideia, String groupId) {
        return addIdeia(ideia, groupId, null, null);
    }

    public Ideia addIdeia(Ideia ideia, String groupId, String userId, String userName) {
        if (ideia.getId() != null && ideia.getId().isBlank()) {
            ideia.setId(null);
        }
        if (userId != null && !userId.isBlank() && (ideia.getUserId() == null || ideia.getUserId().isBlank())) {
            ideia.setUserId(userId);
        }
        if (userName != null && !userName.isBlank() && (ideia.getAutor() == null || ideia.getAutor().isBlank())) {
            ideia.setAutor(userName.trim());
        }
        if (groupId != null && !groupId.isBlank() && (ideia.getGroupId() == null || ideia.getGroupId().isBlank())) {
            ideia.setGroupId(groupId);
        }
        if (ideia.getStatus() == null || ideia.getStatus().isBlank()) {
            ideia.setStatus("Enviada");
        }
        if (ideia.getStatusColor() == null) {
            ideia.setStatusColor(0xFFF3F4F6);
        }
        if (ideia.getStatusTextColor() == null) {
            ideia.setStatusTextColor(0xFF6B7280);
        }
        if (ideia.getVotos() == null) {
            ideia.setVotos(0);
        }
        if (ideia.getProgresso() == null) {
            ideia.setProgresso(0.1);
        }
        if (ideia.getEtapa() == null || ideia.getEtapa().isBlank()) {
            ideia.setEtapa("Aguardando triagem");
        }

        Ideia savedIdeia = ideiaRepository.save(ideia);

        try {
            Notificacao notifAll = new Notificacao();
            notifAll.setMensagem("Nova ideia registrada: " + savedIdeia.getTitulo());
            notifAll.setDestinatarioRole("TODOS");
            notifAll.setTipo("NOVA_IDEIA");
            notifAll.setLida(false);
            notifAll.setIdeiaId(savedIdeia.getId());
            notifAll.setDataCriacao(java.time.LocalDateTime.now());
            notificacaoRepository.save(notifAll);
        } catch (Exception ignored) {}

        return savedIdeia;
    }

    public Ideia updateIdeia(String id, Ideia ideiaUpdates) {
        return ideiaRepository.findById(id).map(i -> {
            if(ideiaUpdates.getTitulo() != null && !ideiaUpdates.getTitulo().isBlank()) i.setTitulo(ideiaUpdates.getTitulo());
            if(ideiaUpdates.getDescricao() != null && !ideiaUpdates.getDescricao().isBlank()) i.setDescricao(ideiaUpdates.getDescricao());
            if(ideiaUpdates.getArea() != null && !ideiaUpdates.getArea().isBlank()) i.setArea(ideiaUpdates.getArea());
            if(ideiaUpdates.getStatus() != null && !ideiaUpdates.getStatus().isBlank()) i.setStatus(ideiaUpdates.getStatus());
            if(ideiaUpdates.getStatusColor() != null) i.setStatusColor(ideiaUpdates.getStatusColor());
            if(ideiaUpdates.getStatusTextColor() != null) i.setStatusTextColor(ideiaUpdates.getStatusTextColor());
            if(ideiaUpdates.getEtapa() != null && !ideiaUpdates.getEtapa().isBlank()) i.setEtapa(ideiaUpdates.getEtapa());
            if(ideiaUpdates.getProgresso() != null) i.setProgresso(ideiaUpdates.getProgresso());
            if(ideiaUpdates.getImpacto() != null && !ideiaUpdates.getImpacto().isBlank()) i.setImpacto(ideiaUpdates.getImpacto());
            if(ideiaUpdates.getObjetivo() != null && !ideiaUpdates.getObjetivo().isBlank()) i.setObjetivo(ideiaUpdates.getObjetivo());
            if(ideiaUpdates.getEstrategiaId() != null) i.setEstrategiaId(ideiaUpdates.getEstrategiaId());
            if(ideiaUpdates.getEstrategiaTitulo() != null) i.setEstrategiaTitulo(ideiaUpdates.getEstrategiaTitulo());
            if(ideiaUpdates.getGroupId() != null && !ideiaUpdates.getGroupId().isBlank()) i.setGroupId(ideiaUpdates.getGroupId());
            return ideiaRepository.save(i);
        }).orElseThrow(() -> new RuntimeException("Ideia nao encontrada"));
    }

    public void deleteIdeia(String id) {
        ideiaRepository.deleteById(id);
    }

    public Ideia votarIdeia(String id) {
        return ideiaRepository.findById(id).map(i -> {
            String status = i.getStatus() != null ? i.getStatus().trim().toUpperCase() : "";
            if (status.contains("APROVAD") || status.contains("RECUSAD")) {
                throw new IllegalStateException("Ideias com status Aprovada ou Recusada não podem mais receber votos.");
            }
            i.setVotos(i.getVotos() != null ? i.getVotos() + 1 : 1);
            return ideiaRepository.save(i);
        }).orElseThrow(() -> new RuntimeException("Ideia nao encontrada"));
    }

    public Ideia comentarIdeia(String id, Comentario comentario) {
        return ideiaRepository.findById(id).map(i -> {
            if (i.getComentarios() == null) i.setComentarios(new java.util.ArrayList<>());
            if (comentario.getDataHora() == null) comentario.setDataHora(java.time.LocalDateTime.now());
            i.getComentarios().add(comentario);
            return ideiaRepository.save(i);
        }).orElseThrow(() -> new RuntimeException("Ideia nao encontrada"));
    }

    // Transacoes Financeiras (Receitas e Despesas)
    public List<TransacaoFinanceira> getTransacoes(String projetoId, String tipo) {
        if (projetoId != null && !projetoId.isEmpty() && tipo != null && !tipo.isEmpty()) {
            return transacaoFinanceiraRepository.findByProjetoIdAndTipoOrderByDataHoraDesc(projetoId, tipo.toUpperCase());
        } else if (projetoId != null && !projetoId.isEmpty()) {
            return transacaoFinanceiraRepository.findByProjetoIdOrderByDataHoraDesc(projetoId);
        } else if (tipo != null && !tipo.isEmpty()) {
            return transacaoFinanceiraRepository.findByTipoOrderByDataHoraDesc(tipo.toUpperCase());
        }
        return transacaoFinanceiraRepository.findAllByOrderByDataHoraDesc();
    }

    public List<TransacaoFinanceira> getReceitas(String projetoId) {
        return getTransacoes(projetoId, "RECEITA");
    }

    public List<TransacaoFinanceira> getDespesas(String projetoId) {
        return getTransacoes(projetoId, "DESPESA");
    }

    public TransacaoFinanceira addTransacao(TransacaoFinanceira transacao) {
        if (transacao.getId() != null && transacao.getId().isEmpty()) {
            transacao.setId(null);
        }
        if (transacao.getDataHora() == null || transacao.getDataHora().isEmpty()) {
            transacao.setDataHora(java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        }
        TransacaoFinanceira saved = transacaoFinanceiraRepository.save(transacao);

        // Se houver projeto vinculado, atualizar metricas e gerar notificacao
        if (transacao.getProjetoId() != null && !transacao.getProjetoId().isEmpty()) {
            projetoRepository.findById(transacao.getProjetoId()).ifPresent(proj -> {
                boolean isReceita = "RECEITA".equalsIgnoreCase(transacao.getTipo());
                if (isReceita) {
                    double novoLucro = (proj.getLucroObtido() != null ? proj.getLucroObtido() : 0.0) + (transacao.getValor() != null ? transacao.getValor() : 0.0);
                    proj.setLucroObtido(novoLucro);
                    projetoRepository.save(proj);
                } else {
                    double invAtual = parseInvestimentoValue(proj);
                    double novoInv = invAtual + (transacao.getValor() != null ? transacao.getValor() : 0.0);
                    proj.setInvestimento(String.format(java.util.Locale.forLanguageTag("pt-BR"), "R$ %,.2f", novoInv));
                    projetoRepository.save(proj);
                }
                
                Notificacao notif = new Notificacao();
                String prefix = isReceita ? "Receita" : "Despesa";
                notif.setMensagem(prefix + " de R$ " + String.format(java.util.Locale.forLanguageTag("pt-BR"), "%,.2f", transacao.getValor()) + " registrada no projeto \"" + proj.getTitulo() + "\"");
                notif.setDestinatarioRole("GESTOR");
                notif.setTipo("FINANCEIRO");
                notif.setLida(false);
                notif.setDataCriacao(java.time.LocalDateTime.now());
                notificacaoRepository.save(notif);
            });
        }
        return saved;
    }

    public TransacaoFinanceira addReceita(TransacaoFinanceira transacao) {
        transacao.setTipo("RECEITA");
        return addTransacao(transacao);
    }

    public TransacaoFinanceira addDespesa(TransacaoFinanceira transacao) {
        transacao.setTipo("DESPESA");
        return addTransacao(transacao);
    }

    public void deleteTransacao(String id) {
        transacaoFinanceiraRepository.findById(id).ifPresent(t -> {
            if (t.getProjetoId() != null && !t.getProjetoId().isEmpty()) {
                projetoRepository.findById(t.getProjetoId()).ifPresent(proj -> {
                    if ("RECEITA".equalsIgnoreCase(t.getTipo())) {
                        double atual = proj.getLucroObtido() != null ? proj.getLucroObtido() : 0.0;
                        proj.setLucroObtido(Math.max(0.0, atual - (t.getValor() != null ? t.getValor() : 0.0)));
                        projetoRepository.save(proj);
                    } else if ("DESPESA".equalsIgnoreCase(t.getTipo())) {
                        double invAtual = parseInvestimentoValue(proj);
                        double novoInv = Math.max(0.0, invAtual - (t.getValor() != null ? t.getValor() : 0.0));
                        proj.setInvestimento(String.format(java.util.Locale.forLanguageTag("pt-BR"), "R$ %,.2f", novoInv));
                        projetoRepository.save(proj);
                    }
                });
            }
        });
        transacaoFinanceiraRepository.deleteById(id);
    }

    public double parseInvestimentoValue(Projeto p) {
        if (p == null) return 0.0;
        if (p.getValorMensal() != null && p.getValorMensal() > 0) {
            double meses = 1.0;
            if (p.getDuracao() != null) {
                String dDigits = p.getDuracao().replaceAll("[^\\d]", "");
                if (!dDigits.isEmpty()) {
                    try {
                        meses = Double.parseDouble(dDigits);
                    } catch (Exception e) {}
                }
            }
            return p.getValorMensal() * meses;
        }
        if (p.getInvestimento() != null && !p.getInvestimento().trim().isEmpty()) {
            String clean = p.getInvestimento()
                    .replace("R$", "")
                    .replace("/mês", "")
                    .replace("/mes", "")
                    .trim();
            String numStr = clean.replaceAll("[^0-9,.]", "");
            try {
                if (numStr.contains(",") && numStr.contains(".")) {
                    return Double.parseDouble(numStr.replace(".", "").replace(",", "."));
                } else if (numStr.contains(",")) {
                    return Double.parseDouble(numStr.replace(",", "."));
                } else if (!numStr.isEmpty()) {
                    return Double.parseDouble(numStr);
                }
            } catch (Exception e) {}
        }
        return 0.0;
    }

    public br.com.inovagab.dto.response.DashboardResumoResponse getDashboardResumo() {
        return getDashboardResumo(null);
    }

    public br.com.inovagab.dto.response.DashboardResumoResponse getDashboardResumo(String groupId) {
        if (groupId == null || groupId.isBlank()) {
            return br.com.inovagab.dto.response.DashboardResumoResponse.builder()
                    .roiTotalPercentual(0.0)
                    .lucroObtidoTotal(0.0)
                    .investimentoTotal(0.0)
                    .projetosAtivos(0)
                    .projetosNoPrazo(0)
                    .ideiasRegistradas(0L)
                    .taxaEngajamento(0.0)
                    .aumentoMedioProdutividade(0.0)
                    .retornosPorEstrategia(new java.util.ArrayList<>())
                    .build();
        }

        List<Projeto> projetos = projetoRepository.findByGroupId(groupId);
        long ideiasRegistradas = ideiaRepository.countByGroupId(groupId);
        List<TransacaoFinanceira> transacoesAvulsas = transacaoFinanceiraRepository.findByGroupIdOrderByDataHoraDesc(groupId);
        
        double lucroTotal = 0.0;
        double investimentoTotal = 0.0;
        int projetosAtivos = 0;
        int projetosNoPrazo = 0;
        double somaProdutividade = 0.0;

        java.util.Map<String, br.com.inovagab.dto.response.DashboardResumoResponse.RetornoPorEstrategia> mapaRetornos = new java.util.HashMap<>();

        for (Projeto p : projetos) {
            if (!"Concluido".equalsIgnoreCase(p.getStatus())) {
                projetosAtivos++;
            }
            if (p.getNoPrazo() != null && p.getNoPrazo()) {
                projetosNoPrazo++;
            }
            if (p.getLucroObtido() != null) {
                lucroTotal += p.getLucroObtido();
            }
            if (p.getAumentoProdutividade() != null) {
                somaProdutividade += p.getAumentoProdutividade();
            }
            
            double invProj = parseInvestimentoValue(p);
            investimentoTotal += invProj;

            if (p.getEstrategiaId() != null && !p.getEstrategiaId().isEmpty()) {
                br.com.inovagab.dto.response.DashboardResumoResponse.RetornoPorEstrategia ret = mapaRetornos.getOrDefault(p.getEstrategiaId(), 
                    new br.com.inovagab.dto.response.DashboardResumoResponse.RetornoPorEstrategia(p.getEstrategiaId(), p.getEstrategiaTitulo() != null ? p.getEstrategiaTitulo() : "Sem Titulo", 0, 0.0, 0.0, 0.0));
                
                ret.setTotalProjetos(ret.getTotalProjetos() + 1);
                ret.setInvestimentoTotal(ret.getInvestimentoTotal() + invProj);
                if (p.getLucroObtido() != null) {
                    ret.setRetornoTotal(ret.getRetornoTotal() + p.getLucroObtido());
                }
                if (ret.getInvestimentoTotal() > 0) {
                    ret.setRoi(((ret.getRetornoTotal() - ret.getInvestimentoTotal()) / ret.getInvestimentoTotal()) * 100);
                }
                mapaRetornos.put(p.getEstrategiaId(), ret);
            }
        }

        // Incluir transacoes financeiras avulsas (sem projeto vinculado) para somar no dashboard
        for (TransacaoFinanceira t : transacoesAvulsas) {
            if (t.getProjetoId() == null || t.getProjetoId().trim().isEmpty()) {
                if ("DESPESA".equalsIgnoreCase(t.getTipo()) && t.getValor() != null) {
                    investimentoTotal += t.getValor();
                } else if ("RECEITA".equalsIgnoreCase(t.getTipo()) && t.getValor() != null) {
                    lucroTotal += t.getValor();
                }
            }
        }

        double roiTotal = 0.0;
        if (investimentoTotal > 0) {
            roiTotal = ((lucroTotal - investimentoTotal) / investimentoTotal) * 100;
        } else if (lucroTotal > 0) {
            roiTotal = 100.0;
        }

        double aumentoMedio = projetos.size() > 0 ? somaProdutividade / projetos.size() : 0.0;
        double taxaEngajamento = projetosAtivos > 0 ? ((double) ideiasRegistradas / projetosAtivos) * 10 : 0.0;

        return br.com.inovagab.dto.response.DashboardResumoResponse.builder()
                .roiTotalPercentual(roiTotal)
                .lucroObtidoTotal(lucroTotal)
                .investimentoTotal(investimentoTotal)
                .projetosAtivos(projetosAtivos)
                .projetosNoPrazo(projetosNoPrazo)
                .ideiasRegistradas(ideiasRegistradas)
                .taxaEngajamento(taxaEngajamento)
                .aumentoMedioProdutividade(aumentoMedio)
                .retornosPorEstrategia(new java.util.ArrayList<>(mapaRetornos.values()))
                .build();
    }
}
