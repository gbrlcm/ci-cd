package br.com.inovagab.service;

import br.com.inovagab.dto.request.GrupoRequest;
import br.com.inovagab.dto.request.MembroRequest;
import br.com.inovagab.dto.request.RegisterRequest;
import br.com.inovagab.dto.response.DashboardResumoResponse;
import br.com.inovagab.dto.response.GrupoResponse;
import br.com.inovagab.model.Estrategia;
import br.com.inovagab.model.Projeto;
import br.com.inovagab.model.Usuario;
import br.com.inovagab.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class GrupoServiceTest {

    @Autowired
    private GrupoService grupoService;

    @Autowired
    private AuthService authService;

    @Autowired
    private InovacaoService inovacaoService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private GrupoRepository grupoRepository;

    @Autowired
    private ProjetoRepository projetoRepository;

    @Autowired
    private EstrategiaRepository estrategiaRepository;

    @BeforeEach
    void setUp() {
        // Limpa coleções de teste
        grupoRepository.deleteAll();
        usuarioRepository.deleteAll();
        projetoRepository.deleteAll();
        estrategiaRepository.deleteAll();
    }

    @Test
    void testCriarGrupoEDelegarRoles() {
        // 1. Cria usuário Gestor
        Usuario gestor = new Usuario();
        gestor.setNome("Gestor");
        gestor.setSobrenome("Silva");
        gestor.setEmail("gestor@aguiabranca.com.br");
        gestor.setSenha("123456");
        gestor.setRole("GESTOR");
        gestor = usuarioRepository.save(gestor);

        // 2. Cria usuário comum (OPERADOR) pré-existente
        Usuario colab = new Usuario();
        colab.setNome("Lucas");
        colab.setSobrenome("Mendes");
        colab.setEmail("lucas@aguiabranca.com.br");
        colab.setSenha("123456");
        colab.setRole("OPERADOR");
        colab = usuarioRepository.save(colab);

        // 3. Gestor cria o grupo e delega LIDER para Lucas e OPERADOR para outro email pendente
        GrupoRequest request = new GrupoRequest();
        request.setNome("Inovação TI");
        request.setDepartamento("Tecnologia");
        request.setDescricao("Projetos estratégicos de TI");
        request.setMembros(List.of(
                new MembroRequest("lucas@aguiabranca.com.br", "LIDER"),
                new MembroRequest("novo@aguiabranca.com.br", "OPERADOR")
        ));

        GrupoResponse grupo = grupoService.criarGrupo(request, gestor.getId());

        assertNotNull(grupo.getHashId());
        assertTrue(grupo.getHashId().startsWith("GRP-"));
        assertEquals("Inovação TI", grupo.getNome());
        assertEquals(3, grupo.getTotalMembros()); // Gestor + Lucas + Novo pendente

        // 4. Verifica se a role de Lucas no MongoDB foi atualizada para LIDER e o groupId foi preenchido
        Usuario lucasAtualizado = usuarioRepository.findById(colab.getId()).orElseThrow();
        assertEquals("LIDER", lucasAtualizado.getRole());
        assertEquals(grupo.getHashId(), lucasAtualizado.getGroupId());

        // 5. Verifica se o Gestor também recebeu o groupId
        Usuario gestorAtualizado = usuarioRepository.findById(gestor.getId()).orElseThrow();
        assertEquals(grupo.getHashId(), gestorAtualizado.getGroupId());

        // 6. Registra o usuário que estava pendente e confere se ele herda a role delegada e o groupId automaticamente
        RegisterRequest regReq = new RegisterRequest();
        regReq.setNome("Novo");
        regReq.setSobrenome("Colaborador");
        regReq.setEmail("novo@aguiabranca.com.br");
        regReq.setPassword("senha123");
        regReq.setUnidade("Vitória");
        authService.registerUser(regReq);

        Usuario novoCadastrado = usuarioRepository.findByEmail("novo@aguiabranca.com.br").orElseThrow();
        assertEquals("OPERADOR", novoCadastrado.getRole());
        assertEquals(grupo.getHashId(), novoCadastrado.getGroupId());
    }

    @Test
    void testIsolamentoDeProjetosEstrategiasEDashboardPorGroupId() {
        String grupoA = "GRP-AAAA1111";
        String grupoB = "GRP-BBBB2222";

        // Cria projeto no Grupo A
        Projeto pA = new Projeto();
        pA.setTitulo("Projeto Alpha");
        pA.setStatus("Em Andamento");
        pA.setInvestimento("R$ 50.000,00");
        pA.setLucroObtido(80000.0);
        pA.setGroupId(grupoA);
        pA.setNoPrazo(true);
        projetoRepository.save(pA);

        // Cria projeto no Grupo B
        Projeto pB = new Projeto();
        pB.setTitulo("Projeto Beta");
        pB.setStatus("Em Andamento");
        pB.setInvestimento("R$ 200.000,00");
        pB.setLucroObtido(10000.0);
        pB.setGroupId(grupoB);
        pB.setNoPrazo(false);
        projetoRepository.save(pB);

        // Cria estratégia no Grupo A
        Estrategia eA = new Estrategia();
        eA.setTitulo("Estratégia A");
        eA.setGroupId(grupoA);
        estrategiaRepository.save(eA);

        // Cria estratégia no Grupo B
        Estrategia eB = new Estrategia();
        eB.setTitulo("Estratégia B");
        eB.setGroupId(grupoB);
        estrategiaRepository.save(eB);

        // 1. Consulta projetos filtrando pelo Grupo A
        List<Projeto> projetosA = inovacaoService.getAllProjetos(grupoA);
        assertEquals(1, projetosA.size());
        assertEquals("Projeto Alpha", projetosA.get(0).getTitulo());

        // 2. Consulta estratégias filtrando pelo Grupo A
        List<Estrategia> estrategiasA = inovacaoService.getAllEstrategias(grupoA);
        assertEquals(1, estrategiasA.size());
        assertEquals("Estratégia A", estrategiasA.get(0).getTitulo());

        // 3. Consulta Dashboard do Grupo A
        DashboardResumoResponse dashA = inovacaoService.getDashboardResumo(grupoA);
        assertEquals(1, dashA.getProjetosAtivos());
        assertEquals(1, dashA.getProjetosNoPrazo());
        assertEquals(80000.0, dashA.getLucroObtidoTotal());
        assertEquals(50000.0, dashA.getInvestimentoTotal());

        // 4. Consulta Dashboard do Grupo B
        DashboardResumoResponse dashB = inovacaoService.getDashboardResumo(grupoB);
        assertEquals(1, dashB.getProjetosAtivos());
        assertEquals(0, dashB.getProjetosNoPrazo());
        assertEquals(10000.0, dashB.getLucroObtidoTotal());
        assertEquals(200000.0, dashB.getInvestimentoTotal());
    }

    @Test
    void testRemoverMembroDoGrupo() {
        Usuario gestor = new Usuario();
        gestor.setNome("Gestor");
        gestor.setEmail("gestor2@aguiabranca.com.br");
        gestor.setSenha("123456");
        gestor.setRole("GESTOR");
        gestor = usuarioRepository.save(gestor);

        Usuario colab = new Usuario();
        colab.setNome("Mariana");
        colab.setEmail("mariana@aguiabranca.com.br");
        colab.setSenha("123456");
        colab.setRole("OPERADOR");
        colab = usuarioRepository.save(colab);

        GrupoRequest request = new GrupoRequest();
        request.setNome("Grupo Marketing");
        request.setDepartamento("Marketing");
        request.setMembros(List.of(new MembroRequest("mariana@aguiabranca.com.br", "LIDER")));

        GrupoResponse grupo = grupoService.criarGrupo(request, gestor.getId());

        // Mariana agora é LIDER e tem groupId
        Usuario marianaAtualizada = usuarioRepository.findById(colab.getId()).orElseThrow();
        assertEquals("LIDER", marianaAtualizada.getRole());
        assertEquals(grupo.getHashId(), marianaAtualizada.getGroupId());

        // Remove Mariana do grupo
        grupoService.removerMembro(grupo.getHashId(), "mariana@aguiabranca.com.br", gestor.getId());

        Usuario marianaPosRemocao = usuarioRepository.findById(colab.getId()).orElseThrow();
        assertNull(marianaPosRemocao.getGroupId());
        assertEquals("OPERADOR", marianaPosRemocao.getRole());
    }

    @Test
    void testProtecaoContraIDOREmGrupoEProjetos() {
        // 1. Cria Gestor e Grupo legítimo
        Usuario gestor = new Usuario();
        gestor.setNome("Gestor A");
        gestor.setEmail("gestorA@empresa.com");
        gestor.setSenha("123456");
        gestor.setRole("GESTOR");
        gestor = usuarioRepository.save(gestor);

        GrupoRequest request = new GrupoRequest();
        request.setNome("Grupo Seguro");
        request.setDepartamento("Segurança");
        GrupoResponse grupo = grupoService.criarGrupo(request, gestor.getId());

        // 2. Cria usuário de fora do grupo (atacante em potencial)
        Usuario atacante = new Usuario();
        atacante.setNome("Atacante");
        atacante.setEmail("atacante@empresa.com");
        atacante.setSenha("123456");
        atacante.setRole("OPERADOR");
        Usuario atacanteSalvo = usuarioRepository.save(atacante);

        // Tentativa de ler dados do grupo pelo hashId por alguém que não é membro nem gestor
        assertThrows(RuntimeException.class, () -> {
            grupoService.obterGrupoPorHash(grupo.getHashId(), atacanteSalvo.getId());
        });

        // 3. Cria projeto no Grupo Seguro
        Projeto p = new Projeto();
        p.setTitulo("Projeto Confidencial");
        p.setGroupId(grupo.getHashId());
        p = projetoRepository.save(p);

        final String projId = p.getId();

        // Tentativa de alterar projeto pertencente ao grupo seguro passando groupId diferente
        Projeto updates = new Projeto();
        updates.setTitulo("Tentativa de Alteração");
        assertThrows(RuntimeException.class, () -> {
            inovacaoService.updateProjeto(projId, updates, "GRP-INVASOR");
        });

        // Tentativa de deletar projeto do grupo seguro passando groupId diferente
        assertThrows(RuntimeException.class, () -> {
            inovacaoService.deleteProjeto(projId, "GRP-INVASOR");
        });

        // Usuário sem groupId não visualiza nada
        List<Projeto> semGrupo = inovacaoService.getAllProjetos(null);
        assertTrue(semGrupo.isEmpty());
    }

    @Test
    void testLoginComSenhaTextoPuroEAutoMigracaoBCrypt() {
        // Simula usuário cadastrado manualmente no MongoDB Compass com senha em texto puro "admin123"
        Usuario usuarioCompass = new Usuario();
        usuarioCompass.setNome("Admin");
        usuarioCompass.setEmail("admin.compass@empresa.com");
        usuarioCompass.setSenha("admin123"); // Sem hash BCrypt!
        usuarioCompass.setRole("GESTOR");
        usuarioCompass = usuarioRepository.save(usuarioCompass);

        // Tentativa de login
        br.com.inovagab.dto.request.LoginRequest req = new br.com.inovagab.dto.request.LoginRequest();
        req.setEmail("admin.compass@empresa.com");
        req.setPassword("admin123");
        req.setSelectedProfile("GESTOR");

        var response = authService.authenticateUser(req);
        assertNotNull(response.getToken());
        assertEquals("GESTOR", response.getRole());

        // Confere se o sistema auto-migrou a senha para um hash BCrypt válido no banco
        Usuario posLogin = usuarioRepository.findById(usuarioCompass.getId()).orElseThrow();
        assertNotEquals("admin123", posLogin.getSenha());
        assertTrue(posLogin.getSenha().startsWith("$2a$") || posLogin.getSenha().startsWith("$2b$"));
    }
}
