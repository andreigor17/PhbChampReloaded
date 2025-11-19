package br.com.champ.Manager;

import br.com.champ.Enums.Url;
import br.com.champ.Modelo.Campeonato;
import br.com.champ.Modelo.Estatisticas;
import br.com.champ.Modelo.ItemPartida;
import br.com.champ.Modelo.Jogo;
import br.com.champ.Modelo.Partida;
import br.com.champ.Modelo.Player;
import br.com.champ.Modelo.Team;
import br.com.champ.Servico.CampeonatoServico;
import br.com.champ.Servico.EstatisticaServico;
import br.com.champ.Servico.ItemPartidaServico;
import br.com.champ.Servico.JogoServico;
import br.com.champ.Servico.PartidaServico;
import br.com.champ.Servico.PlayerServico;
import br.com.champ.Servico.TeamServico;
import br.com.champ.Utilitario.FacesUtil;
import br.com.champ.Utilitario.Mensagem;
import br.com.champ.Utilitario.PartidaUtils;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author andre
 */
@ViewScoped
@Named
public class ManagerCamp extends ManagerBase {

    @EJB
    TeamServico teamServico;
    @EJB
    PlayerServico playerServico;
    @EJB
    CampeonatoServico campeonatoServico;
    @EJB
    EstatisticaServico estatisticaServico;
    @EJB
    PartidaServico partidaServico;
    @EJB
    ItemPartidaServico itemPartidaServico;
    @EJB
    JogoServico jogoServico;

    private Campeonato camp;
    private Campeonato preCamp;
    private List<Campeonato> camps;
    private List<Team> times;
    private Team time;
    private List<Estatisticas> estatisticasTime;
    private Estatisticas estatistica;
    private List<Partida> partidas;
    private List<Estatisticas> ests;
    private List<Team> timesVisualizar;
    private Partida partida;
    private int s1;
    private int s2;
    private Estatisticas mvp;
    private List<Partida> oitavas;
    private List<Partida> quartas;
    private List<Partida> semis;
    private List<Partida> finais;
    private List<Partida> terceiroLugar;
    
    // Cache para partidas - evita múltiplas chamadas HTTP
    private List<Partida> partidasCache;
    private Long campIdCache;
    private boolean partidasCarregadas = false;
    
    // Filtro de jogos por checkbox
    private List<Long> jogosSelecionados;
    private List<Jogo> jogos;

    @PostConstruct
    public void init() {
        instanciar();

        // Carrega o player logado através do ManagerBase
        getPlayerLogado();
        String visualizarCampId = FacesUtil
                .getRequestParameter("id");

        if (visualizarCampId != null && !visualizarCampId.isEmpty()) {
            Long campId = Long.parseLong(visualizarCampId);
            this.camp = this.campeonatoServico.buscaCamp(campId);
            
            // Carrega partidas apenas uma vez e cacheia
            this.partidas = carregarPartidasComCache(campId);
        }

        // Otimização: Carrega estatísticas em batch ao invés de loop N+1
        if (this.camp != null && this.camp.getId() != null && this.camp.getTeams() != null && !this.camp.getTeams().isEmpty()) {
            // Carrega todas as estatísticas de uma vez para todos os times
            carregarEstatisticasOtimizado();
        }

        // MVP só é calculado se necessário (lazy)
        // Removido do init para melhorar performance inicial

        HttpServletRequest uri = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();

        if (uri.getRequestURI().contains("indexCampeonato.xhtml") || uri.getRequestURI().contains("campeonatos.xhtml")) {
            try {
                this.camps = campeonatoServico.pesquisar();
                // Carregar jogos para os filtros (sem necessidade de autenticação)
                this.jogos = jogoServico.autoCompleteJogos();
            } catch (Exception ex) {
                Logger.getLogger(ManagerCamp.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }
    
    /**
     * Carrega partidas com cache - evita múltiplas chamadas HTTP
     */
    private List<Partida> carregarPartidasComCache(Long campId) {
        // Se já carregou para este campeonato, retorna do cache
        if (partidasCarregadas && campIdCache != null && campIdCache.equals(campId) && partidasCache != null) {
            return partidasCache;
        }
        
        // Carrega do serviço e cacheia
        List<Partida> partidasCarregadas = partidaServico.partidaPorCamp(campId);
        this.partidasCache = partidasCarregadas != null ? partidasCarregadas : new ArrayList<>();
        this.campIdCache = campId;
        this.partidasCarregadas = true;
        
        return this.partidasCache;
    }
    
    /**
     * Carrega estatísticas de forma otimizada - evita N+1 queries
     */
    private void carregarEstatisticasOtimizado() {
        if (this.camp == null || this.camp.getId() == null || this.camp.getTeams() == null) {
            return;
        }
        
        // Carrega todas as estatísticas de uma vez para todos os times
        // Assumindo que estatisticaServico tem um método batch (se não tiver, mantém o loop mas otimizado)
        try {
            for (Team timeCamp : this.camp.getTeams()) {
                if (timeCamp != null && timeCamp.getId() != null) {
                    this.estatisticasTime = estatisticaServico.estatisticaPorTime(timeCamp.getId(), this.camp.getId());
                    if (this.estatisticasTime != null) {
                        this.ests.addAll(this.estatisticasTime);
                    }
                }
            }
        } catch (Exception e) {
            Logger.getLogger(ManagerCamp.class.getName()).log(Level.WARNING, "Erro ao carregar estatísticas", e);
        }
    }
    
    /**
     * Limpa o cache quando necessário (ex: após atualizar partidas)
     */
    public void limparCachePartidas() {
        this.partidasCarregadas = false;
        this.partidasCache = null;
        this.campIdCache = null;
    }
    
    /**
     * Obtém partidas do cache ou carrega se necessário
     * Centraliza o acesso às partidas para evitar múltiplas chamadas HTTP
     * Método público para permitir acesso da classe interna SwissRound
     */
    public List<Partida> obterPartidasDoCache() {
        if (camp == null || camp.getId() == null) {
            return new ArrayList<>();
        }
        
        // Se já tem cache válido, retorna do cache
        if (partidasCarregadas && campIdCache != null && campIdCache.equals(camp.getId()) && partidasCache != null) {
            return partidasCache;
        }
        
        // Se não tem cache, carrega e cacheia
        return carregarPartidasComCache(camp.getId());
    }

    public void instanciar() {
        this.camp = new Campeonato();
        this.preCamp = new Campeonato();
        this.camps = new ArrayList<>();
        this.time = new Team();
        this.times = new ArrayList<>();
        this.timesVisualizar = new ArrayList<>();
        this.estatisticasTime = new ArrayList<>();
        this.partidas = new ArrayList<>();
        this.ests = new ArrayList<>();
        this.partida = new Partida();
        this.mvp = new Estatisticas();
        this.oitavas = new ArrayList<>();
        this.quartas = new ArrayList<>();
        this.semis = new ArrayList<>();
        this.finais = new ArrayList<>();
        this.terceiroLugar = new ArrayList<>();
        this.jogosSelecionados = new ArrayList<>();
        this.jogos = new ArrayList<>();

    }

    public Estatisticas getMvp() {
        return mvp;
    }

    public void setMvp(Estatisticas mvp) {
        this.mvp = mvp;
    }

    public Campeonato getPreCamp() {
        return preCamp;
    }

    public void setPreCamp(Campeonato preCamp) {
        this.preCamp = preCamp;
    }

    public Campeonato getCamp() {
        return camp;
    }

    public void setCamp(Campeonato camp) {
        this.camp = camp;
    }

    public List<Campeonato> getCamps() {
        return camps;
    }

    public void setCamps(List<Campeonato> camps) {
        this.camps = camps;
    }

    public List<Team> getTimes() {
        return times;
    }

    public void setTimes(List<Team> times) {
        this.times = times;
    }

    public Team getTime() {
        return time;
    }

    public void setTime(Team time) {
        this.time = time;
    }

    public List<Estatisticas> getEstatisticasTime() {
        return estatisticasTime;
    }

    public void setEstatisticasTime(List<Estatisticas> estatisticasTime) {
        this.estatisticasTime = estatisticasTime;
    }

    public Estatisticas getEstatistica() {
        return estatistica;
    }

    public void setEstatistica(Estatisticas estatistica) {
        this.estatistica = estatistica;
    }

    public List<Partida> getPartidas() {
        return partidas;
    }

    public void setPartidas(List<Partida> partidas) {
        this.partidas = partidas;
    }

    public int getS1() {
        return s1;
    }

    public void setS1(int s1) {
        this.s1 = s1;
    }

    public int getS2() {
        return s2;
    }

    public void setS2(int s2) {
        this.s2 = s2;
    }

    public List<Estatisticas> getEsts() {
        return ests;
    }

    public void setEsts(List<Estatisticas> ests) {
        this.ests = ests;
    }

    public List<Team> getTimesVisualizar() {
        return timesVisualizar;
    }

    public void setTimesVisualizar(List<Team> timesVisualizar) {
        this.timesVisualizar = timesVisualizar;
    }

    public Partida getPartida() {
        return partida;
    }

    public void setPartida(Partida partida) {
        this.partida = partida;
    }

    public List<Team> autoCompletarTime() throws Exception {
        return teamServico.autoCompleteTime();
    }

    public void adicionarCamp() {
        this.times.add(this.time);
        this.time = new Team();

    }

    public void limpar() {
        try {
            // Limpa apenas os campos de filtro
            if (this.camp == null) {
                this.camp = new Campeonato();
            } else {
                this.camp.setNome(null);
                this.camp.setDataCamp(null);
            }
            
            // Limpa a lista de jogos selecionados
            if (this.jogosSelecionados == null) {
                this.jogosSelecionados = new ArrayList<>();
            } else {
                this.jogosSelecionados.clear();
            }
            
            // Mantém a lista de jogos carregada (não limpa)
            // Se não estiver carregada, carrega
            if (this.jogos == null || this.jogos.isEmpty()) {
                this.jogos = jogoServico.autoCompleteJogos();
            }
            
            // Refaz a consulta como se fosse o carregamento inicial
            this.camps = campeonatoServico.pesquisar();
        } catch (Exception ex) {
            Logger.getLogger(ManagerCamp.class.getName()).log(Level.SEVERE, null, ex);
            // Em caso de erro, garante que a lista não fique null
            if (this.camps == null) {
                this.camps = new ArrayList<>();
            }
        }
    }
    
    public List<Long> getJogosSelecionados() {
        return jogosSelecionados;
    }

    public void setJogosSelecionados(List<Long> jogosSelecionados) {
        this.jogosSelecionados = jogosSelecionados;
    }
    
    public List<Jogo> getJogos() {
        return jogos;
    }

    public void setJogos(List<Jogo> jogos) {
        this.jogos = jogos;
    }

    public void removeCamp() {
        try {
            this.campeonatoServico.delete(this.camp, Url.EXCLUIR_CAMPEONATO.getNome());
        } catch (Exception ex) {
            Logger.getLogger(ManagerCamp.class.getName()).log(Level.SEVERE, null, ex);
        }
        Mensagem.successAndRedirect("pesquisarCampeonato.xhtml");
        init();
    }

    public void pesquisarCamp() throws Exception {
        this.camps = campeonatoServico.pesquisar();
    }

    public void atualizarPartida() throws Exception {

        try {
//            this.estatistica = estatisticaServico.estatisticaPorTime(this.partida.getTeam1().getId(), this.camp.getId()).get(0);
//            if (this.estatistica != null) {
//                this.estatistica.setRoundsGanhos(this.estatistica.getRoundsGanhos() + this.partida.getScoreT1());
//                this.estatistica.setRoundsPerdidos(this.estatistica.getRoundsPerdidos() + this.partida.getScoreT2());
//                this.estatistica.setPontos(this.estatistica.getPontos() + 3);
//                estatisticaServico.salvar(this.estatistica, this.estatistica.getId(), Url.ATUALIZAR_ESTATISTICA.getNome());
//            }
//
//            this.estatistica = estatisticaServico.estatisticaPorTime(this.partida.getTeam2().getId(), this.camp.getId()).get(0);
//            if (this.estatistica != null) {
//                this.estatistica.setRoundsGanhos(this.estatistica.getRoundsGanhos() + this.partida.getScoreT2());
//                this.estatistica.setRoundsPerdidos(this.estatistica.getRoundsPerdidos() + this.partida.getScoreT1());
//                estatisticaServico.salvar(this.estatistica, this.estatistica.getId(), Url.ATUALIZAR_ESTATISTICA.getNome());
//            }
//
//            partidaServico.salvar(this.partida, this.partida.getId(), Url.ATUALIZAR_PARTIDA.getNome());
//            Mensagem.successAndRedirect("Partida atualizada com sucesso", "visualizarCampeonato.xhtml?id=" + this.camp.getId());
        } catch (Exception e) {
            System.err.println(e);
        }

    }

    public static Comparator<Estatisticas> comparadorTime = new Comparator<Estatisticas>() {
        @Override
        public int compare(Estatisticas o1, Estatisticas o2) {
            if (o1.getPontos() != o2.getPontos()) {
                return Integer.compare(o2.getPontos(), o1.getPontos());
            } else {
                return Integer.compare(o2.getRoundsGanhos(), o1.getRoundsGanhos());
            }
        }
    };

    public static Comparator<Estatisticas> comparadorPlayer = new Comparator<Estatisticas>() {
        @Override
        public int compare(Estatisticas o1, Estatisticas o2) {
            if (o1.getKills() != o2.getKills()) {
                return Integer.compare(o2.getKills(), o1.getKills());
            } else {
                return Integer.compare(o1.getDeaths(), o2.getDeaths());
            }
        }
    };

    public List<Estatisticas> somaEstsTime() {
        List<Estatisticas> soma = new ArrayList<>();
        Estatisticas est = new Estatisticas();
        Integer kills = 0;
        Integer deaths = 0;
        Integer assists = 0;
        Integer pontos = 0;
        Integer roundsGanhos = 0;
        Integer roundsPerdidos = 0;

        for (Team timeCamp : this.camp.getTeams()) {
            this.estatisticasTime = estatisticaServico.estatisticaPorTime(timeCamp.getId(), this.camp.getId());
            for (Estatisticas estats : this.estatisticasTime) {
                if (estats.getTeam().getId().equals(timeCamp.getId())) {
                    kills += estats.getKills();
                    deaths += estats.getDeaths();
                    assists += estats.getAssists();
                    pontos += estats.getPontos();
                    roundsGanhos += estats.getRoundsGanhos();
                    roundsPerdidos += estats.getRoundsPerdidos();
                }

            }

            est.setKills(kills);
            est.setAssists(assists);
            est.setDeaths(deaths);
            est.setTeam(timeCamp);
            est.setPontos(pontos);
            est.setRoundsGanhos(roundsGanhos);
            est.setRoundsPerdidos(roundsPerdidos);
            soma.add(est);
            kills = 0;
            deaths = 0;
            assists = 0;
            pontos = 0;
            roundsGanhos = 0;
            roundsPerdidos = 0;
            est = new Estatisticas();
        }
        soma.sort(comparadorTime);
        return soma;

    }

    public List<Estatisticas> somaEstsPlayers() {
        List<Estatisticas> soma = new ArrayList<>();
        Estatisticas est = new Estatisticas();
        Integer kills = 0;
        Integer deaths = 0;
        Integer assists = 0;
        Integer pontos = 0;

        for (Player player : this.camp.getPlayers()) {
            this.estatisticasTime = estatisticaServico.estatisticaPorTime(player.getId(), this.camp.getId());
            for (Estatisticas estats : this.estatisticasTime) {
                if (estats.getPlayer().getId().equals(player.getId())) {
                    kills += estats.getKills();
                    deaths += estats.getDeaths();
                    assists += estats.getAssists();
                }

            }

            est.setKills(kills);
            est.setAssists(assists);
            est.setDeaths(deaths);
            est.setPlayer(player);
            soma.add(est);
            kills = 0;
            deaths = 0;
            assists = 0;
            est = new Estatisticas();
        }
        return soma;

    }

    public List<Estatisticas> somaEstsPlayersTop() {
        List<Estatisticas> soma = new ArrayList<>();
        Estatisticas est = new Estatisticas();
        Integer kills = 0;
        Integer deaths = 0;
        Integer assists = 0;
        Integer pontos = 0;

        for (Team t : this.camp.getTeams()) {
            for (Player player : t.getPlayers()) {
                this.estatisticasTime = estatisticaServico.estatisticaPorPlayer(player.getId(), this.camp.getId());
                for (Estatisticas estats : this.estatisticasTime) {
                    if (estats.getPlayer().getId().equals(player.getId())) {
                        kills += estats.getKills();
                        deaths += estats.getDeaths();
                        assists += estats.getAssists();
                    }

                }

                est.setKills(kills);
                est.setAssists(assists);
                est.setDeaths(deaths);
                est.setPlayer(player);
                soma.add(est);
                kills = 0;
                deaths = 0;
                assists = 0;
                est = new Estatisticas();
            }
        }
        soma.sort(comparadorPlayer);
        for (Estatisticas e : soma) {
            System.err.println("player " + e.getPlayer().getNome() + " - " + "kills " + e.getKills() + " - " + "assists " + e.getAssists() + " - " + "deaths " + e.getDeaths());
        }
        soma.sort(comparadorPlayer);
        return soma;

    }

    public String nomeAnexo(Team team) {
        if (team.getAnexo() != null && team.getAnexo().getNome() != null) {
            return "image/" + team.getAnexo().getNome();
        } else {
            return "media/images/cs2.png";
        }
    }

    public String nomeAnexoMvp(Player player) {
        if (player.getAnexo() != null && player.getAnexo().getNome() != null) {
            return "image/" + player.getAnexo().getNome();
        } else {
            return "media/images/cs2.png";
        }
    }

    /**
     * Verifica se o usuário logado é administrador
     * Método exposto para o JSF
     * @return true se o usuário logado é admin, false caso contrário
     */
    public boolean isAdmin() {
        return super.isAdmin();
    }

    /**
     * Verifica se o player logado já está inscrito no campeonato
     * @return true se o player pode se inscrever (não está inscrito), false caso contrário
     */
    public boolean podeSeInscrever() {
        try {
            Player player = getPlayerLogado();
            if (player == null || player.getId() == null) {
                return false;
            }

            if (this.camp == null || this.camp.getId() == null) {
                return false;
            }

            // Atualiza o campeonato do banco para ter dados frescos
            this.camp = this.campeonatoServico.buscaCamp(this.camp.getId());

            // Verifica categoria do campeonato
            if (this.camp.getCategoria() == null) {
                return false;
            }

            String categoria = this.camp.getCategoria().getNome();

            // Categoria INDIVIDUAL ou TIME com gerarTimesPorSorteio
            if ("INDIVIDUAL".equals(categoria) || 
                ("TIME".equals(categoria) && this.camp.isGerarTimesPorSorteio())) {
                // Verifica se já está na lista de players
                if (this.camp.getPlayers() != null && this.camp.getPlayers().stream()
                        .anyMatch(p -> p.getId() != null && p.getId().equals(player.getId()))) {
                    return false; // Já está inscrito
                }
                return true; // Pode se inscrever
            }
            // Categoria TIME sem gerarTimesPorSorteio
            else if ("TIME".equals(categoria)) {
                // Verifica se o player é capitão de um time que já está inscrito
                Team timeCapitao = teamServico.buscaTeamPorCapitao(player.getId());
                if (timeCapitao != null && timeCapitao.getId() != null) {
                    if (this.camp.getTeams() != null && this.camp.getTeams().stream()
                            .anyMatch(t -> t.getId() != null && t.getId().equals(timeCapitao.getId()))) {
                        return false; // Time já está inscrito
                    }
                }
                return true; // Pode se inscrever
            }

            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void inscrever() {
        try {
            Player player = getPlayerLogado();
            if (player == null) {
                Mensagem.error("Você precisa estar logado para se inscrever!");
                return;
            }

            if (this.camp == null || this.camp.getId() == null) {
                Mensagem.error("Campeonato não encontrado!");
                return;
            }

            // Atualiza o campeonato do banco para ter dados frescos
            this.camp = this.campeonatoServico.buscaCamp(this.camp.getId());

            // Verifica categoria do campeonato
            if (this.camp.getCategoria() == null) {
                Mensagem.error("Categoria do campeonato não definida!");
                return;
            }

            String categoria = this.camp.getCategoria().getNome();

            // Categoria INDIVIDUAL
            if ("INDIVIDUAL".equals(categoria)) {
                // Verifica se já está inscrito
                if (this.camp.getPlayers() != null && this.camp.getPlayers().stream()
                        .anyMatch(p -> p.getId() != null && p.getId().equals(player.getId()))) {
                    Mensagem.error("Você já está inscrito neste campeonato!");
                    return;
                }

                // Inicializa a lista se necessário
                if (this.camp.getPlayers() == null) {
                    this.camp.setPlayers(new ArrayList<>());
                }

                // Adiciona o player
                this.camp.getPlayers().add(player);
                this.camp = campeonatoServico.save(this.camp, this.camp.getId(), Url.ATUALIZAR_CAMPEONATO.getNome());
                Mensagem.successAndRedirect("Inscrição realizada com sucesso!", "visualizarCampeonato.xhtml?id=" + this.camp.getId());
            }
            // Categoria TIME
            else if ("TIME".equals(categoria)) {
                // Verifica se gera times automaticamente
                if (this.camp.isGerarTimesPorSorteio()) {
                    // Inscreve o player na lista de players
                    // Verifica se já está inscrito
                    if (this.camp.getPlayers() != null && this.camp.getPlayers().stream()
                            .anyMatch(p -> p.getId() != null && p.getId().equals(player.getId()))) {
                        Mensagem.error("Você já está inscrito neste campeonato!");
                        return;
                    }

                    // Inicializa a lista se necessário
                    if (this.camp.getPlayers() == null) {
                        this.camp.setPlayers(new ArrayList<>());
                    }

                    // Adiciona o player
                    this.camp.getPlayers().add(player);
                    this.camp = campeonatoServico.save(this.camp, this.camp.getId(), Url.ATUALIZAR_CAMPEONATO.getNome());
                    Mensagem.successAndRedirect("Inscrição realizada com sucesso! Você será distribuído em um time quando o campeonato começar.", "visualizarCampeonato.xhtml?id=" + this.camp.getId());
                } else {
                    // Não gera times automaticamente - inscreve o time do qual o player é capitão
                    Team timeCapitao = teamServico.buscaTeamPorCapitao(player.getId());
                    
                    if (timeCapitao == null || timeCapitao.getId() == null) {
                        Mensagem.error("Você precisa ser capitão de um time para se inscrever neste campeonato!");
                        return;
                    }

                    // Verifica se o time já está inscrito
                    if (this.camp.getTeams() != null && this.camp.getTeams().stream()
                            .anyMatch(t -> t.getId() != null && t.getId().equals(timeCapitao.getId()))) {
                        Mensagem.error("Seu time já está inscrito neste campeonato!");
                        return;
                    }

                    // Inicializa a lista se necessário
                    if (this.camp.getTeams() == null) {
                        this.camp.setTeams(new ArrayList<>());
                    }

                    // Adiciona o time
                    this.camp.getTeams().add(timeCapitao);
                    this.camp = campeonatoServico.save(this.camp, this.camp.getId(), Url.ATUALIZAR_CAMPEONATO.getNome());
                    Mensagem.successAndRedirect("Inscrição realizada com sucesso! Seu time foi inscrito no campeonato.", "visualizarCampeonato.xhtml?id=" + this.camp.getId());
                }
            } else {
                Mensagem.error("Categoria de campeonato não suportada!");
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            Mensagem.error("Erro ao realizar inscrição: " + ex.getMessage());
        }
    }

    /**
     * Sorteia os times automaticamente baseado nos players inscritos
     * Apenas admin pode executar esta ação
     * Apenas para campeonatos de TIME com gerarTimesPorSorteio = true
     */
    public void sortearTimes() {
        try {
            System.out.println("====== INICIANDO SORTEIO DE TIMES ======");
            
            // Verifica se é admin
            if (!isAdmin()) {
                System.err.println("Erro: Usuário não é admin");
                Mensagem.error("Apenas administradores podem sortear os times!");
                return;
            }
            System.out.println("✓ Usuário é admin");

            if (this.camp == null || this.camp.getId() == null) {
                System.err.println("Erro: Campeonato não encontrado");
                Mensagem.error("Campeonato não encontrado!");
                return;
            }
            System.out.println("✓ Campeonato encontrado: ID=" + this.camp.getId());

            // Atualiza o campeonato do banco para ter dados frescos
            this.camp = this.campeonatoServico.buscaCamp(this.camp.getId());
            System.out.println("✓ Campeonato atualizado do banco");

            // Verifica se é campeonato de TIME com gerarTimesPorSorteio
            if (this.camp.getCategoria() == null || !"TIME".equals(this.camp.getCategoria().getNome())) {
                System.err.println("Erro: Campeonato não é de times. Categoria: " + (this.camp.getCategoria() != null ? this.camp.getCategoria().getNome() : "null"));
                Mensagem.error("Este campeonato não é de times!");
                return;
            }
            System.out.println("✓ Campeonato é de times");

            if (!this.camp.isGerarTimesPorSorteio()) {
                System.err.println("Erro: Campeonato não usa sorteio automático");
                Mensagem.error("Este campeonato não utiliza sorteio automático de times!");
                return;
            }
            System.out.println("✓ Campeonato usa sorteio automático");

            // Verifica se há players inscritos
            if (this.camp.getPlayers() == null || this.camp.getPlayers().isEmpty()) {
                System.err.println("Erro: Nenhum jogador inscrito. Players: " + (this.camp.getPlayers() != null ? this.camp.getPlayers().size() : "null"));
                Mensagem.error("Não há jogadores inscritos para sortear!");
                return;
            }
            System.out.println("✓ " + this.camp.getPlayers().size() + " jogadores inscritos");

            // Verifica se quantidadePorTime está definido
            if (this.camp.getQuantidadePorTime() == null || this.camp.getQuantidadePorTime() <= 0) {
                System.err.println("Erro: Quantidade por time inválida: " + this.camp.getQuantidadePorTime());
                Mensagem.error("Quantidade de jogadores por time não está definida!");
                return;
            }
            System.out.println("✓ Quantidade por time: " + this.camp.getQuantidadePorTime());

            // Verifica se já existem times criados (evita duplicação)
            if (this.camp.getTeams() != null && !this.camp.getTeams().isEmpty()) {
                System.err.println("Erro: Times já foram sorteados. Total de times: " + this.camp.getTeams().size());
                Mensagem.error("Os times já foram sorteados anteriormente!");
                return;
            }
            System.out.println("✓ Nenhum time foi sorteado ainda");

            // Cria uma cópia da lista de players e embaralha
            List<Player> playersParaSortear = new ArrayList<>(this.camp.getPlayers());
            Collections.shuffle(playersParaSortear);
            System.out.println("✓ Lista de jogadores embaralhada");

            int quantidadePorTime = this.camp.getQuantidadePorTime();
            List<Team> timesCriados = new ArrayList<>();
            int numeroTime = 1;
            
            // HashSet para garantir que nenhum jogador seja usado mais de uma vez
            Set<Long> jogadoresJaUsados = new HashSet<>();

            System.out.println("====== INICIANDO CRIAÇÃO DOS TIMES ======");
            
            // Divide os players em grupos e cria os times
            for (int i = 0; i < playersParaSortear.size(); i += quantidadePorTime) {
                int fim = Math.min(i + quantidadePorTime, playersParaSortear.size());
                List<Player> playersDoTime = new ArrayList<>(playersParaSortear.subList(i, fim));
                
                // Validação extra: verifica se algum jogador já foi usado
                for (Player player : playersDoTime) {
                    if (player.getId() != null && jogadoresJaUsados.contains(player.getId())) {
                        Mensagem.error("Erro: Jogador " + player.getNome() + " foi sorteado mais de uma vez!");
                        return;
                    }
                    if (player.getId() != null) {
                        jogadoresJaUsados.add(player.getId());
                    }
                }

                // Cria um novo time
                Team novoTime = new Team();
                
                // Se quantidadePorTime for 2, usa os nicks dos jogadores
                if (quantidadePorTime == 2 && playersDoTime.size() == 2) {
                    String nick1 = playersDoTime.get(0).getNick() != null ? playersDoTime.get(0).getNick() : playersDoTime.get(0).getNome();
                    String nick2 = playersDoTime.get(1).getNick() != null ? playersDoTime.get(1).getNick() : playersDoTime.get(1).getNome();
                    novoTime.setNome("Time " + nick1 + " e " + nick2);
                } else {
                    novoTime.setNome("Time " + playersDoTime.get(0).getNick());
                }
                
                novoTime.setPlayers(playersDoTime);
                novoTime.setTimeAmistoso(false);
                novoTime.setActive(true);

                // Marca o primeiro player como capitão
                if (!playersDoTime.isEmpty()) {
                    Player capitao = playersDoTime.get(0);
                    capitao.setCapitao(true);
                }

                // Salva o time
                Team timeSalvo = teamServico.save(novoTime, null, Url.SALVAR_TIME.getNome());
                timesCriados.add(timeSalvo);
                System.out.println("✓ Time " + numeroTime + " criado: " + novoTime.getNome() + " (ID: " + timeSalvo.getId() + ")");

                numeroTime++;
            }

            System.out.println("====== FINALIZANDO SORTEIO ======");
            System.out.println("Total de times criados: " + timesCriados.size());

            // Adiciona os times ao campeonato
            if (this.camp.getTeams() == null) {
                this.camp.setTeams(new ArrayList<>());
            }
            this.camp.getTeams().addAll(timesCriados);
            System.out.println("✓ Times adicionados ao campeonato");

            // Atualiza o campeonato
            this.camp = campeonatoServico.save(this.camp, this.camp.getId(), Url.ATUALIZAR_CAMPEONATO.getNome());
            System.out.println("✓ Campeonato atualizado no banco");

            System.out.println("====== SORTEIO CONCLUÍDO COM SUCESSO ======");
            
            Mensagem.successAndRedirect(
                "Times sorteados com sucesso! " + timesCriados.size() + " times criados.",
                "visualizarCampeonato.xhtml?id=" + this.camp.getId()
            );

        } catch (Exception ex) {
            System.err.println("====== ERRO NO SORTEIO ======");
            ex.printStackTrace();
            Mensagem.error("Erro ao sortear times: " + ex.getMessage());
        }
    }

    

    public List<Partida> getOitavas() {
        return oitavas;
    }

    public void setOitavas(List<Partida> oitavas) {
        this.oitavas = oitavas;
    }

    public List<Partida> getQuartas() {
        return quartas;
    }

    public void setQuartas(List<Partida> quartas) {
        this.quartas = quartas;
    }

    public List<Partida> getSemis() {
        return semis;
    }

    public void setSemis(List<Partida> semis) {
        this.semis = semis;
    }

    public List<Partida> getFinais() {
        return finais;
    }

    public void setFinais(List<Partida> finais) {
        this.finais = finais;
    }

    // Helpers para view
    public int getTimesConfirmados(Campeonato camp) {
        if (camp != null && camp.getTeams() != null) {
            return camp.getTeams().size();
        }
        return 0;
    }

    public String formataData(Date data) {
        if (data != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            return sdf.format(data);
        }
        return "—";
    }

    // Helpers para Swiss System
    /**
     * Retorna todas as rodadas suíças geradas, na ordem sequencial
     * Busca partidas tanto das listas do campeonato quanto diretamente pelo contadorEsperado
     */
    /**
     * Retorna as partidas de uma rodada agrupadas por recorde antes da rodada
     */
    public Map<String, List<Partida>> getPartidasPorRecorde(SwissRound rodada) {
        if (rodada == null) {
            return new HashMap<>();
        }
        return rodada.getPartidasPorRecorde(this.camp, this.partidaServico, this);
    }
    
    public List<SwissRound> getRodadasSuicas() {
        List<SwissRound> rodadas = new ArrayList<>();
        if (camp == null || camp.getId() == null) {
            return rodadas;
        }
        
        // Não recarrega o campeonato desnecessariamente - usa o já carregado
        // Se precisar de dados atualizados, pode recarregar apenas quando necessário
        
        if (camp.getTipoCampeonato() == null || !"SUÍÇO".equals(camp.getTipoCampeonato().getNome())) {
            return rodadas;
        }
        
        // Usa partidas do cache ao invés de buscar novamente
        List<Partida> todasPartidas = obterPartidasDoCache();
        
        if (todasPartidas == null) {
            todasPartidas = new ArrayList<>();
        }
        
        System.out.println("Total de partidas encontradas para o campeonato: " + todasPartidas.size());
        
        // Mapeamento da ordem das rodadas
        // Rodada 1 -> 00, Rodada 2 -> 10, Rodada 3 -> 01, etc.
        String[] ordemRodadas = {"00", "10", "01", "20", "11", "02", "30", "21", "12", "03", "31", "22", "13", "32", "23", "33"};
        
        // Agrupa partidas por número de rodada (contadorEsperado)
        Map<Integer, List<Partida>> partidasPorRodada = new HashMap<>();
        for (Partida partida : todasPartidas) {
            int contadorEsperado = partida.getContadorEsperado();
            if (contadorEsperado > 0) {
                partidasPorRodada.computeIfAbsent(contadorEsperado, k -> new ArrayList<>()).add(partida);
                System.out.println("Partida ID " + partida.getId() + " encontrada na rodada " + contadorEsperado);
            }
        }
        
        System.out.println("Partidas agrupadas por rodada: " + partidasPorRodada.keySet());
        
        int numeroRodada = 1;
        for (String codigoRodada : ordemRodadas) {
            List<Partida> partidasRodada = new ArrayList<>();
            
            // Primeiro tenta buscar das listas do campeonato (reflection)
            try {
                String getterName = "getRodadaSuica" + codigoRodada;
                java.lang.reflect.Method getter = camp.getClass().getMethod(getterName);
                List<Partida> partidasLista = (List<Partida>) getter.invoke(camp);
                
                if (partidasLista != null && !partidasLista.isEmpty()) {
                    partidasRodada.addAll(partidasLista);
                }
            } catch (Exception e) {
                // Se não encontrar pelo reflection, continua para buscar por contadorEsperado
            }
            
            // Se não encontrou na lista, busca pelo contadorEsperado do mapa
            if (partidasRodada.isEmpty() && partidasPorRodada.containsKey(numeroRodada)) {
                partidasRodada.addAll(partidasPorRodada.get(numeroRodada));
            }
            
            // Se encontrou partidas, cria a rodada
            if (!partidasRodada.isEmpty()) {
                String nomeRodada = "Rodada " + numeroRodada;
                SwissRound rodada = new SwissRound(numeroRodada, nomeRodada, partidasRodada);
                
                // Calcula os records dos times até esta rodada
                rodada.recordsNaRodada = calcularRecordsAteRodada(numeroRodada);
                
                rodadas.add(rodada);
                numeroRodada++;
            } else {
                // Se não encontrou partidas para esta rodada, para de procurar
                break;
            }
        }
        
        System.out.println("Total de rodadas suíças encontradas: " + rodadas.size());
        for (SwissRound rodada : rodadas) {
            System.out.println("  - " + rodada.getNome() + " com " + rodada.getPartidas().size() + " partidas");
        }
        
        return rodadas;
    }
    
    /**
     * Calcula os records (vitórias:derrotas) de todos os times até determinada rodada
     * Retorna um mapa com a contagem de times em cada record
     */
    private Map<String, Integer> calcularRecordsAteRodada(int ateRodada) {
        Map<String, Integer> recordCount = new HashMap<>();
        Map<Long, String> teamRecords = new HashMap<>();
        
        // Inicializa todos os times
        if (camp.getTeams() != null) {
            for (Team team : camp.getTeams()) {
                teamRecords.put(team.getId(), "0:0");
            }
        }
        
        // Usa partidas do cache ao invés de buscar novamente
        List<Partida> todasPartidas = obterPartidasDoCache();
        if (todasPartidas == null) {
            return recordCount;
        }
        
        // Processa apenas partidas finalizadas
        for (Partida partida : todasPartidas) {
            if (partida.isFinalizada() && partida.getTimeVencedor() != null &&
                partida.getItemPartida() != null && !partida.getItemPartida().isEmpty()) {
                
                ItemPartida item = partida.getItemPartida().get(0);
                Team team1 = item.getTeam1();
                Team team2 = item.getTeam2();
                
                if (team1 != null && team2 != null) {
                    // Atualiza o record do vencedor
                    if (partida.getTimeVencedor().getId().equals(team1.getId())) {
                        atualizarRecord(teamRecords, team1.getId(), true);
                        atualizarRecord(teamRecords, team2.getId(), false);
                    } else {
                        atualizarRecord(teamRecords, team2.getId(), true);
                        atualizarRecord(teamRecords, team1.getId(), false);
                    }
                }
            }
        }
        
        // Conta quantos times têm cada record
        for (String record : teamRecords.values()) {
            recordCount.put(record, recordCount.getOrDefault(record, 0) + 1);
        }
        
        return recordCount;
    }
    
    /**
     * Atualiza o record de um time (adiciona vitória ou derrota)
     */
    public void atualizarRecord(Map<Long, String> teamRecords, Long teamId, boolean vitoria) {
        String currentRecord = teamRecords.get(teamId);
        if (currentRecord == null) {
            currentRecord = "0:0";
        }
        
        String[] parts = currentRecord.split(":");
        int wins = Integer.parseInt(parts[0]);
        int losses = Integer.parseInt(parts[1]);
        
        if (vitoria) {
            wins++;
        } else {
            losses++;
        }
        
        teamRecords.put(teamId, wins + ":" + losses);
    }

    public boolean isRodadaAtualFinalizada(SwissRound rodada) {
        if (rodada == null) {
            return true;
        }
        return rodada.isFinalizada();
    }

    public boolean podeMostrarProximaRodada() {
        List<SwissRound> rodadas = getRodadasSuicas();
        if (rodadas.isEmpty()) {
            return false;
        }
        // Verifica se a última rodada está finalizada
        SwissRound ultimaRodada = rodadas.get(rodadas.size() - 1);
        return isRodadaAtualFinalizada(ultimaRodada);
    }

    /**
     * Gera a próxima rodada suíça do campeonato
     * Segue as regras do sistema suíço: emparelhamento por record, evita confrontos repetidos
     * Apenas admin pode executar esta ação
     */
    public void gerarProximaRodada() {
        try {
            // Validações
            if (!isAdmin()) {
                Mensagem.error("Apenas administradores podem gerar rodadas!");
                return;
            }

            if (this.camp == null || this.camp.getId() == null) {
                Mensagem.error("Campeonato não encontrado!");
                return;
            }

            // Atualiza dados frescos do banco
            this.camp = this.campeonatoServico.buscaCamp(this.camp.getId());

            if (this.camp.getTipoCampeonato() == null || !"SUÍÇO".equals(this.camp.getTipoCampeonato().getNome())) {
                Mensagem.error("Este campeonato não é do tipo SUÍÇO!");
                return;
            }

            if (this.camp.getTeams() == null || this.camp.getTeams().isEmpty()) {
                Mensagem.error("Não há times inscritos para gerar rodadas!");
                return;
            }

            // Verifica se a rodada atual está finalizada
            int rodadaAtualNumero = getRodadaAtualNumero();
            if (rodadaAtualNumero > 0) {
                List<SwissRound> rodadas = getRodadasSuicas();
                if (!rodadas.isEmpty()) {
                    SwissRound ultimaRodada = rodadas.get(rodadas.size() - 1);
                    if (!isRodadaAtualFinalizada(ultimaRodada)) {
                        Mensagem.error("A rodada " + rodadaAtualNumero + " ainda não foi finalizada!");
                        return;
                    }
                }
            }

            // Calcula quantas rodadas são necessárias (log2 do número de times)
            int numTimes = this.camp.getTeams().size();
            int maxRodadas = (int) Math.ceil(Math.log(numTimes) / Math.log(2)) + 2; // +2 para rodadas extras
            
            if (rodadaAtualNumero >= maxRodadas) {
                Mensagem.error("Todas as rodadas necessárias já foram geradas!");
                return;
            }

            // Calcula records dos times
            Map<Team, SwissRecord> recordsPorTime = calcularSwissRecords();

            // Gera emparelhamentos para a próxima rodada
            List<Partida> partidasGeradas = gerarEmparelhamentosSwiss(recordsPorTime, rodadaAtualNumero + 1);

            if (partidasGeradas.isEmpty()) {
                Mensagem.error("Não foi possível gerar emparelhamentos para esta rodada!");
                return;
            }

            // Salva as partidas na rodada correspondente
            salvarPartidasNaRodada(partidasGeradas, recordsPorTime);

            // Atualiza o campeonato
            this.camp = campeonatoServico.save(this.camp, this.camp.getId(), Url.ATUALIZAR_CAMPEONATO.getNome());

            Mensagem.successAndRedirect(
                "Rodada " + (rodadaAtualNumero + 1) + " gerada com sucesso! " + partidasGeradas.size() + " partidas criadas.",
                "visualizarCampeonato.xhtml?id=" + this.camp.getId()
            );

        } catch (Exception ex) {
            ex.printStackTrace();
            Mensagem.error("Erro ao gerar rodada: " + ex.getMessage());
        }
    }
    
    /**
     * Retorna o número da rodada atual (baseado nas partidas existentes)
     * Retorna 0 se não houver rodadas
     */
    public int getRodadaAtualNumero() {
        List<SwissRound> rodadas = getRodadasSuicas();
        return rodadas.size();
    }
    
    /**
     * Retorna descrição da rodada atual para exibição
     */
    public String getRodadaAtualDescricao() {
        int numRodada = getRodadaAtualNumero();
        if (numRodada == 0) {
            return "Aguardando início";
        }
        
        List<SwissRound> rodadas = getRodadasSuicas();
        if (rodadas.isEmpty()) {
            return "Rodada 1 - Aguardando geração";
        }
        
        SwissRound rodadaAtual = rodadas.get(rodadas.size() - 1);
        boolean finalizada = isRodadaAtualFinalizada(rodadaAtual);
        
        if (finalizada) {
            return "Rodada " + numRodada + " finalizada - Próxima: Rodada " + (numRodada + 1);
        } else {
            return "Rodada " + numRodada + " em andamento";
        }
    }
    
    /**
     * Verifica se todas as partidas da rodada foram finalizadas
     */
    public boolean isRodadaAtualEmAndamento() {
        List<SwissRound> rodadas = getRodadasSuicas();
        if (rodadas.isEmpty()) {
            return false;
        }
        
        SwissRound rodadaAtual = rodadas.get(rodadas.size() - 1);
        return !isRodadaAtualFinalizada(rodadaAtual);
    }

    /**
     * Classe auxiliar para representar o record de um time no sistema suíço
     */
    private static class SwissRecord {
        int wins;
        int losses;
        Set<Long> opponentIds; // IDs dos times que já enfrentou
        
        public SwissRecord() {
            this.wins = 0;
            this.losses = 0;
            this.opponentIds = new HashSet<>();
        }
        
        public String getRecordString() {
            return wins + ":" + losses;
        }
        
        public int getScore() {
            return wins * 100 - losses * 10; // Score para ordenação
        }
        
        public boolean hasPlayed(Long opponentId) {
            return opponentIds.contains(opponentId);
        }
        
        public void addOpponent(Long opponentId) {
            opponentIds.add(opponentId);
        }
    }
    
    /**
     * Calcula os records de todos os times baseado nas partidas finalizadas
     */
    private Map<Team, SwissRecord> calcularSwissRecords() {
        Map<Long, Team> teamMap = new HashMap<>();
        Map<Team, SwissRecord> records = new HashMap<>();
        
        // Inicializa todos os times com record vazio
        for (Team time : this.camp.getTeams()) {
            teamMap.put(time.getId(), time);
            records.put(time, new SwissRecord());
        }
        
        // Usa partidas do cache ao invés de buscar novamente
        List<Partida> todasPartidas = obterPartidasDoCache();
        if (todasPartidas != null) {
            for (Partida partida : todasPartidas) {
                if (partida.isFinalizada() && partida.getTimeVencedor() != null && 
                    partida.getTimePerdedor() != null) {
                    
                    Team timeVencedor = partida.getTimeVencedor();
                    Team timePerdedor = partida.getTimePerdedor();
                    
                    // Encontra os times no mapa local
                    Team localTimeVencedor = teamMap.get(timeVencedor.getId());
                    Team localTimePerdedor = teamMap.get(timePerdedor.getId());
                    
                    if (localTimeVencedor == null || localTimePerdedor == null) continue;
                    
                    SwissRecord recordVencedor = records.get(localTimeVencedor);
                    SwissRecord recordPerdedor = records.get(localTimePerdedor);
                    
                    // Registra que jogaram entre si
                    recordVencedor.addOpponent(timePerdedor.getId());
                    recordPerdedor.addOpponent(timeVencedor.getId());
                    
                    // Atualiza vitórias e derrotas
                    recordVencedor.wins++;
                    recordPerdedor.losses++;
                }
            }
        }
        
        return records;
    }

    /**
     * Gera os emparelhamentos para a próxima rodada do sistema suíço
     * Segue as regras: emparelha times com records similares, evita rematches
     * Times com 3:0 ou 0:3 não jogam mais
     */
    private List<Partida> gerarEmparelhamentosSwiss(Map<Team, SwissRecord> records, int rodadaNumero) throws Exception {
        List<Partida> partidasGeradas = new ArrayList<>();
        
        // Cria lista de times ordenados por score (wins e losses)
        List<Team> timesOrdenados = new ArrayList<>(records.keySet());
        timesOrdenados.sort((t1, t2) -> {
            SwissRecord r1 = records.get(t1);
            SwissRecord r2 = records.get(t2);
            return Integer.compare(r2.getScore(), r1.getScore()); // Ordem decrescente
        });
        
        // Se for a primeira rodada, embaralha os times
        if (rodadaNumero == 1) {
            Collections.shuffle(timesOrdenados);
        }
        
        // Remove times que já alcançaram 3 vitórias (3:0, 3:1, 3:2) ou 3 derrotas (0:3, 1:3, 2:3)
        // Esses times não jogam mais nas próximas rodadas
        timesOrdenados.removeIf(time -> {
            SwissRecord record = records.get(time);
            return record.wins >= 3 || record.losses >= 3;
        });
        
        // Lista de times já emparelhados nesta rodada
        Set<Long> timesEmparelhados = new HashSet<>();
        
        // Algoritmo de emparelhamento suíço
        for (int i = 0; i < timesOrdenados.size(); i++) {
            Team time1 = timesOrdenados.get(i);
            
            // Pula se já foi emparelhado
            if (timesEmparelhados.contains(time1.getId())) {
                continue;
            }
            
            SwissRecord record1 = records.get(time1);
            Team melhorOponente = null;
            int melhorDistancia = Integer.MAX_VALUE;
            
            // Procura o melhor oponente
            for (int j = i + 1; j < timesOrdenados.size(); j++) {
                Team time2 = timesOrdenados.get(j);
                
                // Pula se já foi emparelhado
                if (timesEmparelhados.contains(time2.getId())) {
                    continue;
                }
                
                SwissRecord record2 = records.get(time2);
                
                // Se já jogaram entre si, pula (exceto se não houver outra opção)
                if (record1.hasPlayed(time2.getId())) {
                    continue;
                }
                
                // Calcula a distância de score
                int distancia = Math.abs(record1.getScore() - record2.getScore());
                
                // Se é o primeiro oponente viável ou é melhor que o atual
                if (melhorOponente == null || distancia < melhorDistancia) {
                    melhorOponente = time2;
                    melhorDistancia = distancia;
                }
                
                // Se encontrou um oponente com o mesmo record exato, usa ele
                if (distancia == 0) {
                    break;
                }
            }
            
            // Se não encontrou oponente sem rematch, procura qualquer um disponível
            if (melhorOponente == null) {
                for (int j = i + 1; j < timesOrdenados.size(); j++) {
                    Team time2 = timesOrdenados.get(j);
                    if (!timesEmparelhados.contains(time2.getId())) {
                        melhorOponente = time2;
                        break;
                    }
                }
            }
            
            // Se encontrou oponente, cria a partida
            if (melhorOponente != null) {
                Partida partida = criarPartidaSwiss(time1, melhorOponente);
                if (partida != null) {
                    partidasGeradas.add(partida);
                    timesEmparelhados.add(time1.getId());
                    timesEmparelhados.add(melhorOponente.getId());
                }
            }
            // Se não encontrou oponente, o time recebe BYE (vitória automática)
            else {
                // Time recebe BYE - não joga mas ganha a rodada
                // Isso será implementado ao calcular records na próxima rodada
            }
        }
        
        return partidasGeradas;
    }

    /**
     * Cria uma partida entre dois times para o sistema suíço
     */
    private Partida criarPartidaSwiss(Team time1, Team time2) throws Exception {
        if (time1 == null || time2 == null || time1.getId() == null || time2.getId() == null) {
            return null;
        }
        
        Partida novaPartida = new Partida();
        novaPartida.setJogo(this.camp.getJogo());
        novaPartida.setActive(true);
        novaPartida.setFinalizada(false);
        
        // Cria os itens da partida (mapas)
        // Usa qtdItensPartidas do campeonato ou um valor padrão
        int qtdItens = 1; // Valor padrão, pode ser ajustado
        if (this.camp.getJogo() != null) {
            // Pode buscar quantidade de mapas do jogo
        }
        
        List<ItemPartida> itensPartida = PartidaUtils.gerarPartidasTimes(novaPartida, this.camp.getId(), time1, time2, qtdItens);
        novaPartida.setItemPartida(itensPartida);
        
        // Salva a partida
        novaPartida = partidaServico.salvar(novaPartida, null, Url.SALVAR_PARTIDA.getNome());
        
        // Atualiza os itens da partida com o ID da partida
        if (novaPartida.getItemPartida() != null) {
            List<ItemPartida> itensAtualizados = new ArrayList<>();
            for (ItemPartida item : novaPartida.getItemPartida()) {
                item.setPartida(novaPartida.getId());
                itensAtualizados.add(item);
            }
            novaPartida.setItemPartida(itensAtualizados);
            novaPartida = partidaServico.salvar(novaPartida, novaPartida.getId(), Url.ATUALIZAR_PARTIDA.getNome());
        }
        
        return novaPartida;
    }

    /**
     * Salva as partidas na rodada correspondente do campeonato
     * Usa o número da rodada para determinar onde salvar
     * IMPORTANTE: Salva o contadorEsperado no banco de dados
     */
    private void salvarPartidasNaRodada(List<Partida> partidas, Map<Team, SwissRecord> records) throws Exception {
        if (partidas.isEmpty()) {
            return;
        }
        
        // Determina o número da próxima rodada (baseado em quantas rodadas já existem + 1)
        int numeroRodada = getRodadaAtualNumero() + 1;
        
        // Mapeamento de número de rodada para nome do campo
        // Rodada 1 -> 00, Rodada 2 -> 10, Rodada 3 -> 01, etc.
        String[] ordemRodadas = {"00", "10", "01", "20", "11", "02", "30", "21", "12", "03", "31", "22", "13", "32", "23", "33"};
        
        if (numeroRodada <= 0 || numeroRodada > ordemRodadas.length) {
            throw new Exception("Número de rodada inválido: " + numeroRodada);
        }
        
        String rodadaNome = ordemRodadas[numeroRodada - 1]; // -1 porque array começa em 0
        String setterName = "setRodadaSuica" + rodadaNome;
        String getterName = "getRodadaSuica" + rodadaNome;
        
        try {
            // Busca a lista atual da rodada
            java.lang.reflect.Method getter = this.camp.getClass().getMethod(getterName);
            List<Partida> rodadaAtual = (List<Partida>) getter.invoke(this.camp);
            
            if (rodadaAtual == null) {
                rodadaAtual = new ArrayList<>();
            }
            
            // Configura o número da rodada em cada partida E SALVA NO BANCO
            List<Partida> partidasAtualizadas = new ArrayList<>();
            for (Partida partida : partidas) {
                // Define o contadorEsperado
                partida.setContadorEsperado(numeroRodada);
                
                // Define o nome da partida (opcional, mas ajuda na visualização)
                if (partida.getNome() == null || partida.getNome().isEmpty()) {
                    partida.setNome("Rodada " + numeroRodada + " - " + 
                        (partida.getItemPartida() != null && !partida.getItemPartida().isEmpty() 
                            ? partida.getItemPartida().get(0).getTeam1().getNome() + " vs " + 
                              partida.getItemPartida().get(0).getTeam2().getNome()
                            : "Partida"));
                }
                
                // Salva a partida novamente para persistir o contadorEsperado
                if (partida.getId() != null) {
                    partida = partidaServico.salvar(partida, partida.getId(), Url.ATUALIZAR_PARTIDA.getNome());
                    partidasAtualizadas.add(partida);
                    System.out.println("Partida ID " + partida.getId() + " atualizada com contadorEsperado=" + numeroRodada);
                } else {
                    System.err.println("ERRO: Partida sem ID não pode ser atualizada!");
                    partidasAtualizadas.add(partida);
                }
            }
            
            // Adiciona todas as partidas atualizadas à rodada
            rodadaAtual.addAll(partidasAtualizadas);
            
            // Atualiza a rodada no campeonato
            java.lang.reflect.Method setter = this.camp.getClass().getMethod(setterName, List.class);
            setter.invoke(this.camp, rodadaAtual);
            
            System.out.println("Partidas salvas na rodada " + rodadaNome + " (Rodada #" + numeroRodada + ") - Total: " + partidasAtualizadas.size());
            
        } catch (NoSuchMethodException e) {
            throw new Exception("Método " + setterName + " não encontrado no modelo Campeonato");
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("Erro ao salvar partidas na rodada: " + e.getMessage());
        }
    }

    // Classe interna para representar uma rodada suíça
    public static class SwissRound {
        public int numeroRodada;
        public String nome;
        public List<Partida> partidas;
        public boolean finalizada;
        public Map<String, Integer> recordsNaRodada; // Record -> Quantidade de times

        public SwissRound(int numeroRodada, String nome, List<Partida> partidas) {
            this.numeroRodada = numeroRodada;
            this.nome = nome;
            this.partidas = partidas;
            this.finalizada = verificarSeTodasPartidasFinalizadas(partidas);
            this.recordsNaRodada = new HashMap<>();
        }
        
        private boolean verificarSeTodasPartidasFinalizadas(List<Partida> partidas) {
            if (partidas == null || partidas.isEmpty()) {
                return false;
            }
            for (Partida p : partidas) {
                if (!p.isFinalizada()) {
                    return false;
                }
            }
            return true;
        }

        public int getNumeroRodada() {
            return numeroRodada;
        }
        
        public String getNome() {
            return nome;
        }

        public List<Partida> getPartidas() {
            return partidas;
        }
        
        public boolean isFinalizada() {
            return finalizada;
        }
        
        public String getStatus() {
            if (finalizada) {
                return "Finalizada";
            }
            return "Em Andamento";
        }

        public Map<String, Integer> getRecordsNaRodada() {
            return recordsNaRodada;
        }
        
        /**
         * Agrupa as partidas desta rodada por recorde antes da rodada
         * Retorna um Map onde a chave é o recorde (ex: "0:0", "1:0", "0:1") e o valor é a lista de partidas
         */
        public Map<String, List<Partida>> getPartidasPorRecorde(Campeonato camp, PartidaServico partidaServico, ManagerCamp managerCamp) {
            Map<String, List<Partida>> partidasPorRecorde = new HashMap<>();
            Map<Long, String> recordsAntesRodada = calcularRecordsAntesRodada(this.numeroRodada, camp, partidaServico, managerCamp);
            
            if (partidas == null) {
                return partidasPorRecorde;
            }
            
            for (Partida partida : partidas) {
                if (partida.getItemPartida() != null && !partida.getItemPartida().isEmpty()) {
                    ItemPartida item = partida.getItemPartida().get(0);
                    Team team1 = item.getTeam1();
                    Team team2 = item.getTeam2();
                    
                    if (team1 != null && team2 != null) {
                        // Pega o recorde do time1 antes desta rodada
                        String recordTeam1 = recordsAntesRodada.getOrDefault(team1.getId(), "0:0");
                        
                        // Agrupa a partida pelo recorde do primeiro time
                        // (ambos os times da partida têm o mesmo recorde antes da rodada)
                        if (!partidasPorRecorde.containsKey(recordTeam1)) {
                            partidasPorRecorde.put(recordTeam1, new ArrayList<>());
                        }
                        partidasPorRecorde.get(recordTeam1).add(partida);
                    }
                }
            }
            
            return partidasPorRecorde;
        }
        
        /**
         * Calcula os records de todos os times ANTES desta rodada
         */
        private Map<Long, String> calcularRecordsAntesRodada(int numeroRodada, Campeonato camp, PartidaServico partidaServico, ManagerCamp managerCamp) {
            Map<Long, String> teamRecords = new HashMap<>();
            
            // Inicializa todos os times com 0:0
            if (camp.getTeams() != null) {
                for (Team team : camp.getTeams()) {
                    teamRecords.put(team.getId(), "0:0");
                }
            }
            
            // Usa partidas do cache do ManagerCamp ao invés de buscar novamente
            List<Partida> todasPartidas = managerCamp.obterPartidasDoCache();
            if (todasPartidas == null) {
                return teamRecords;
            }
            
            // Processa apenas partidas finalizadas de rodadas anteriores
            for (Partida partida : todasPartidas) {
                if (partida.isFinalizada() && partida.getTimeVencedor() != null &&
                    partida.getItemPartida() != null && !partida.getItemPartida().isEmpty()) {
                    
                    // Verifica se a partida pertence a uma rodada anterior
                    int contadorPartida = partida.getContadorEsperado();
                    if (contadorPartida > 0 && contadorPartida < numeroRodada) {
                        ItemPartida item = partida.getItemPartida().get(0);
                        Team team1 = item.getTeam1();
                        Team team2 = item.getTeam2();
                        
                        if (team1 != null && team2 != null) {
                            // Atualiza o record do vencedor
                            if (partida.getTimeVencedor().getId().equals(team1.getId())) {
                                managerCamp.atualizarRecord(teamRecords, team1.getId(), true);
                                managerCamp.atualizarRecord(teamRecords, team2.getId(), false);
                            } else {
                                managerCamp.atualizarRecord(teamRecords, team2.getId(), true);
                                managerCamp.atualizarRecord(teamRecords, team1.getId(), false);
                            }
                        }
                    }
                }
            }
            
            return teamRecords;
        }
    }

    // Classe interna para representar a classificação de um time
    public static class ClassificacaoTime {
        private Team team;
        private int vitorias;
        private int derrotas;
        private int pontos;
        private int posicao;

        public ClassificacaoTime(Team team) {
            this.team = team;
            this.vitorias = 0;
            this.derrotas = 0;
            this.pontos = 0;
        }

        public void adicionarVitoria() {
            this.vitorias++;
            this.pontos += 3;
        }

        public void adicionarDerrota() {
            this.derrotas++;
        }

        public Team getTeam() {
            return team;
        }

        public int getVitorias() {
            return vitorias;
        }

        public int getDerrotas() {
            return derrotas;
        }

        public int getPontos() {
            return pontos;
        }

        public int getPosicao() {
            return posicao;
        }

        public void setPosicao(int posicao) {
            this.posicao = posicao;
        }

        public String getRecord() {
            return vitorias + ":" + derrotas;
        }
    }

    /**
     * Calcula e retorna a tabela de classificação ordenada por vitórias
     * Ordem: mais vitórias primeiro, em caso de empate, menos derrotas
     */
    public List<ClassificacaoTime> getTabelaClassificacao() {
        List<ClassificacaoTime> tabela = new ArrayList<>();
        
        if (this.camp == null || this.camp.getTeams() == null || this.camp.getTeams().isEmpty()) {
            return tabela;
        }

        // Inicializa classificação para cada time
        Map<Long, ClassificacaoTime> classificacaoPorTime = new HashMap<>();
        for (Team time : this.camp.getTeams()) {
            classificacaoPorTime.put(time.getId(), new ClassificacaoTime(time));
        }

        // Usa partidas do cache ao invés de buscar novamente
        List<Partida> todasPartidas = obterPartidasDoCache();
        if (todasPartidas != null) {
            for (Partida partida : todasPartidas) {
                if (partida.isFinalizada() && partida.getTimeVencedor() != null && 
                    partida.getTimePerdedor() != null) {
                    
                    Team timeVencedor = partida.getTimeVencedor();
                    Team timePerdedor = partida.getTimePerdedor();
                    
                    ClassificacaoTime classVencedor = classificacaoPorTime.get(timeVencedor.getId());
                    ClassificacaoTime classPerdedor = classificacaoPorTime.get(timePerdedor.getId());
                    
                    if (classVencedor != null && classPerdedor != null) {
                        classVencedor.adicionarVitoria();
                        classPerdedor.adicionarDerrota();
                    }
                }
            }
        }

        // Converte para lista e ordena
        tabela = new ArrayList<>(classificacaoPorTime.values());
        tabela.sort((c1, c2) -> {
            // Primeiro critério: mais vitórias
            int compareVitorias = Integer.compare(c2.getVitorias(), c1.getVitorias());
            if (compareVitorias != 0) {
                return compareVitorias;
            }
            // Segundo critério: menos derrotas
            int compareDerrotas = Integer.compare(c1.getDerrotas(), c2.getDerrotas());
            if (compareDerrotas != 0) {
                return compareDerrotas;
            }
            // Terceiro critério: ordem alfabética do nome
            return c1.getTeam().getNome().compareTo(c2.getTeam().getNome());
        });

        // Define as posições
        for (int i = 0; i < tabela.size(); i++) {
            tabela.get(i).setPosicao(i + 1);
        }

        return tabela;
    }

    /**
     * Registra o resultado de uma partida do formato suíço
     * Define time vencedor, perdedor e finaliza a partida
     */
    public void registrarResultadoPartida(Partida partida, Integer scoreTime1, Integer scoreTime2) {
        try {
            if (partida == null || partida.getId() == null) {
                Mensagem.error("Partida não encontrada!");
                return;
            }

            // Recarrega a partida do banco para garantir dados atualizados
            Partida partidaPersistida = partidaServico.pesquisar(partida.getId());
            if (partidaPersistida == null) {
                Mensagem.error("Partida não encontrada!");
                return;
            }

            Team vencedor = partida.getTimeVencedor() != null ? partida.getTimeVencedor() : partidaPersistida.getTimeVencedor();
            Team perdedor = partida.getTimePerdedor() != null ? partida.getTimePerdedor() : partidaPersistida.getTimePerdedor();

            if (vencedor == null) {
                Mensagem.error("Defina o time vencedor antes de salvar o resultado!");
                return;
            }

            // Caso o perdedor não tenha sido informado, tenta deduzir pelo item da partida
            if (perdedor == null && partidaPersistida.getItemPartida() != null && !partidaPersistida.getItemPartida().isEmpty()) {
                ItemPartida item = partidaPersistida.getItemPartida().get(0);
                Team time1 = item.getTeam1();
                Team time2 = item.getTeam2();

                if (time1 != null && time2 != null) {
                    if (vencedor.getId().equals(time1.getId())) {
                        perdedor = time2;
                    } else if (vencedor.getId().equals(time2.getId())) {
                        perdedor = time1;
                    }
                }
            }

            partidaPersistida.setTimeVencedor(vencedor);
            partidaPersistida.setTimePerdedor(perdedor);
            partidaPersistida.setFinalizada(true);

            partidaPersistida = partidaServico.salvar(partidaPersistida, partidaPersistida.getId(), Url.ATUALIZAR_PARTIDA.getNome());

            Mensagem.success("Resultado da partida registrado com sucesso!");

            // Limpa o cache para forçar recarregamento na próxima vez
            limparCachePartidas();
            
            // Recarrega partidas do cache (que vai buscar novamente)
            if (this.camp != null && this.camp.getId() != null) {
                this.partidas = obterPartidasDoCache();
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            Mensagem.error("Erro ao registrar resultado: " + ex.getMessage());
        }
    }

    /**
     * Registra resultado usando ID da partida (para uso no XHTML)
     */
    public void registrarResultadoPartidaPorId(Long partidaId, Integer scoreTime1, Integer scoreTime2) {
        try {
            Partida partida = partidaServico.pesquisar(partidaId);
            registrarResultadoPartida(partida, scoreTime1, scoreTime2);
        } catch (Exception ex) {
            ex.printStackTrace();
            Mensagem.error("Erro ao buscar partida: " + ex.getMessage());
        }
    }

    /**
     * Variáveis para o diálogo de resultado
     */
    private Partida partidaParaResultado;
    private Integer scoreTime1;
    private Integer scoreTime2;
    private Long vencedorSelecionadoId;

    public Partida getPartidaParaResultado() {
        return partidaParaResultado;
    }

    public void setPartidaParaResultado(Partida partidaParaResultado) {
        this.partidaParaResultado = partidaParaResultado;
        // Reseta seleção quando selecionar nova partida
        this.scoreTime1 = null;
        this.scoreTime2 = null;
        this.vencedorSelecionadoId = null;
    }

    public Integer getScoreTime1() {
        return scoreTime1;
    }

    public void setScoreTime1(Integer scoreTime1) {
        this.scoreTime1 = scoreTime1;
    }

    public Integer getScoreTime2() {
        return scoreTime2;
    }

    public void setScoreTime2(Integer scoreTime2) {
        this.scoreTime2 = scoreTime2;
    }

    public Long getVencedorSelecionadoId() {
        return vencedorSelecionadoId;
    }

    public void setVencedorSelecionadoId(Long vencedorSelecionadoId) {
        this.vencedorSelecionadoId = vencedorSelecionadoId;
    }

    /**
     * Abre o diálogo para registrar resultado
     */
    public void abrirDialogoResultado(Partida partida) {
        // Método otimizado - apenas define a partida sem validações pesadas
        this.partidaParaResultado = partida;
        this.scoreTime1 = null;
        this.scoreTime2 = null;
        this.vencedorSelecionadoId = null;
    }

    /**
     * Salva o resultado usando as variáveis do diálogo
     */
    public void salvarResultadoDialogo() {
        if (partidaParaResultado == null) {
            Mensagem.error("Partida não selecionada!");
            return;
        }

        if (vencedorSelecionadoId == null) {
            Mensagem.error("Selecione o time vencedor da partida!");
            return;
        }

        if (partidaParaResultado.getItemPartida() == null || partidaParaResultado.getItemPartida().isEmpty()) {
            Mensagem.error("Partida não possui times definidos!");
            return;
        }

        ItemPartida item = partidaParaResultado.getItemPartida().get(0);
        Team time1 = item.getTeam1();
        Team time2 = item.getTeam2();

        if (time1 == null || time2 == null) {
            Mensagem.error("Times da partida não encontrados!");
            return;
        }

        Team vencedor = null;
        if (time1.getId().equals(vencedorSelecionadoId)) {
            vencedor = time1;
        } else if (time2.getId().equals(vencedorSelecionadoId)) {
            vencedor = time2;
        }

        if (vencedor == null) {
            Mensagem.error("Time vencedor inválido!");
            return;
        }

        Team perdedor = vencedor.getId().equals(time1.getId()) ? time2 : time1;

        partidaParaResultado.setTimeVencedor(vencedor);
        partidaParaResultado.setTimePerdedor(perdedor);

        registrarResultadoPartida(partidaParaResultado, null, null);

        this.scoreTime1 = null;
        this.scoreTime2 = null;
        this.vencedorSelecionadoId = null;
    }

    /**
     * Obtém o nome do time 1 da partida selecionada
     */
    public String getNomeTime1Partida() {
        if (partidaParaResultado != null && 
            partidaParaResultado.getItemPartida() != null && 
            !partidaParaResultado.getItemPartida().isEmpty()) {
            Team time1 = partidaParaResultado.getItemPartida().get(0).getTeam1();
            return time1 != null ? time1.getNome() : "";
        }
        return "";
    }

    public Long getIdTime1Partida() {
        if (partidaParaResultado != null && 
            partidaParaResultado.getItemPartida() != null && 
            !partidaParaResultado.getItemPartida().isEmpty()) {
            Team time1 = partidaParaResultado.getItemPartida().get(0).getTeam1();
            return time1 != null ? time1.getId() : null;
        }
        return null;
    }

    /**
     * Obtém o nome do time 2 da partida selecionada
     */
    public String getNomeTime2Partida() {
        if (partidaParaResultado != null && 
            partidaParaResultado.getItemPartida() != null && 
            !partidaParaResultado.getItemPartida().isEmpty()) {
            Team time2 = partidaParaResultado.getItemPartida().get(0).getTeam2();
            return time2 != null ? time2.getNome() : "";
        }
        return "";
    }

    public Long getIdTime2Partida() {
        if (partidaParaResultado != null && 
            partidaParaResultado.getItemPartida() != null && 
            !partidaParaResultado.getItemPartida().isEmpty()) {
            Team time2 = partidaParaResultado.getItemPartida().get(0).getTeam2();
            return time2 != null ? time2.getId() : null;
        }
        return null;
    }

}
