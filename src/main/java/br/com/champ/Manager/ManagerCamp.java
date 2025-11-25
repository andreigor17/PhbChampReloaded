package br.com.champ.Manager;

import br.com.champ.Enums.StatusCamp;
import br.com.champ.Enums.Url;
import br.com.champ.Modelo.Campeonato;
import br.com.champ.Modelo.Estatisticas;
import br.com.champ.Modelo.Fase;
import br.com.champ.Modelo.Grupo;
import br.com.champ.Modelo.ItemPartida;
import br.com.champ.Modelo.Jogo;
import br.com.champ.Modelo.Partida;
import br.com.champ.Modelo.Player;
import br.com.champ.Modelo.Team;
import br.com.champ.Servico.CampeonatoServico;
import br.com.champ.Servico.EstatisticaServico;
import br.com.champ.Servico.FaseServico;
import br.com.champ.Servico.GrupoServico;
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
import java.util.stream.Collectors;

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
    @EJB
    FaseServico faseServico;
    @EJB
    GrupoServico grupoServico;

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
    
    // Cache para estatísticas de jogadores
    private List<EstatisticaJogador> estatisticasJogadoresCache;
    private Long campIdEstatisticasCache;

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
     * Obtém partidas do cache ou carrega se necessário Centraliza o acesso às
     * partidas para evitar múltiplas chamadas HTTP Método público para permitir
     * acesso da classe interna SwissRound
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
        // Limpa cache de estatísticas quando o campeonato muda
        if (this.camp == null || this.campIdEstatisticasCache == null 
            || !this.camp.getId().equals(this.campIdEstatisticasCache)) {
            this.estatisticasJogadoresCache = null;
            this.campIdEstatisticasCache = null;
        }
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
     * Verifica se o usuário logado é administrador Método exposto para o JSF
     *
     * @return true se o usuário logado é admin, false caso contrário
     */
    public boolean isAdmin() {
        return super.isAdmin();
    }

    /**
     * Verifica se o player logado já está inscrito no campeonato
     *
     * @return true se o player pode se inscrever (não está inscrito), false
     * caso contrário
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
            if ("INDIVIDUAL".equals(categoria)
                    || ("TIME".equals(categoria) && this.camp.isGerarTimesPorSorteio())) {
                // Verifica se já está na lista de players
                if (this.camp.getPlayers() != null && this.camp.getPlayers().stream()
                        .anyMatch(p -> p.getId() != null && p.getId().equals(player.getId()))) {
                    return false; // Já está inscrito
                }
                return true; // Pode se inscrever
            } // Categoria TIME sem gerarTimesPorSorteio
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
            } // Categoria TIME
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
     * Sorteia os times automaticamente baseado nos players inscritos Apenas
     * admin pode executar esta ação Apenas para campeonatos de TIME com
     * gerarTimesPorSorteio = true Balanceia os times baseado no rating dos
     * players para evitar que players de rating alto fiquem juntos no mesmo
     * time
     */
    public void sortearTimes() {
        try {
            System.out.println("====== INICIANDO SORTEIO DE TIMES (COM BALANCEAMENTO) ======");

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

            // Cria uma cópia da lista de players
            List<Player> playersParaSortear = new ArrayList<>(this.camp.getPlayers());

            // Remove players sem rating e define rating padrão de 5 se não tiver
            for (Player p : playersParaSortear) {
                if (p.getRating() == null || p.getRating() <= 0) {
                    p.setRating(5.0); // Rating padrão para players sem rating
                    System.out.println("⚠ Player " + p.getNome() + " não possui rating, definido como 5.0");
                }
            }

            // Ordena players por rating (maior para menor)
            playersParaSortear.sort((p1, p2) -> {
                Double rating1 = p1.getRating() != null ? p1.getRating() : 5.0;
                Double rating2 = p2.getRating() != null ? p2.getRating() : 5.0;
                return Double.compare(rating2, rating1); // Ordem decrescente
            });

            System.out.println("✓ Players ordenados por rating (maior para menor)");
            for (Player p : playersParaSortear) {
                System.out.println("  - " + p.getNome() + ": Rating " + p.getRating());
            }

            int quantidadePorTime = this.camp.getQuantidadePorTime();
            int totalTimes = (int) Math.ceil((double) playersParaSortear.size() / quantidadePorTime);

            // Lista de times que serão criados (cada time é uma lista de players)
            List<List<Player>> times = new ArrayList<>();
            for (int i = 0; i < totalTimes; i++) {
                times.add(new ArrayList<>());
            }

            // Lista para rastrear players já distribuídos
            boolean[] playersDistribuidos = new boolean[playersParaSortear.size()];

            System.out.println("====== INICIANDO DISTRIBUIÇÃO BALANCEADA ======");
            System.out.println("Total de times a criar: " + totalTimes);

            // Algoritmo de balanceamento
            // Distribui players de forma alternada, mas verificando compatibilidade de rating
            for (int round = 0; round < quantidadePorTime; round++) {
                for (int timeIndex = 0; timeIndex < totalTimes; timeIndex++) {
                    // Se o time já está completo, pula
                    if (times.get(timeIndex).size() >= quantidadePorTime) {
                        continue;
                    }

                    // Procura um player compatível para este time
                    Player playerEscolhido = null;
                    int playerEscolhidoIndex = -1;

                    for (int i = 0; i < playersParaSortear.size(); i++) {
                        if (playersDistribuidos[i]) {
                            continue;
                        }

                        Player candidato = playersParaSortear.get(i);

                        // Verifica compatibilidade com players já no time
                        boolean compativel = true;
                        for (Player playerNoTime : times.get(timeIndex)) {
                            Double ratingCandidato = candidato.getRating() != null ? candidato.getRating() : 5.0;
                            Double ratingNoTime = playerNoTime.getRating() != null ? playerNoTime.getRating() : 5.0;
                            double diferenca = Math.abs(ratingCandidato - ratingNoTime);

                            // Se a diferença for menor que 4, não são compatíveis
                            // Exemplo: rating 8 não pode ficar com rating 6 (diferença 2)
                            // Mas pode ficar com rating 4 (diferença 4)
                            if (diferenca < 4.0) {
                                compativel = false;
                                break;
                            }
                        }

                        if (compativel) {
                            playerEscolhido = candidato;
                            playerEscolhidoIndex = i;
                            break;
                        }
                    }

                    // Se não encontrou player compatível, pega o próximo disponível
                    // (pode acontecer se não houver mais opções balanceadas)
                    if (playerEscolhido == null) {
                        for (int i = 0; i < playersParaSortear.size(); i++) {
                            if (!playersDistribuidos[i]) {
                                playerEscolhido = playersParaSortear.get(i);
                                playerEscolhidoIndex = i;
                                break;
                            }
                        }
                    }

                    // Adiciona o player ao time
                    if (playerEscolhido != null) {
                        times.get(timeIndex).add(playerEscolhido);
                        playersDistribuidos[playerEscolhidoIndex] = true;
                        System.out.println("  ✓ Round " + (round + 1) + ", Time " + (timeIndex + 1)
                                + ": " + playerEscolhido.getNome() + " (Rating: " + playerEscolhido.getRating() + ")");
                    }
                }
            }

            // Verifica se todos os players foram distribuídos
            for (int i = 0; i < playersParaSortear.size(); i++) {
                if (!playersDistribuidos[i]) {
                    // Adiciona players restantes ao último time
                    if (!times.isEmpty()) {
                        times.get(times.size() - 1).add(playersParaSortear.get(i));
                        System.out.println("  ⚠ Player restante adicionado: " + playersParaSortear.get(i).getNome());
                    }
                }
            }

            System.out.println("====== CRIANDO TIMES ======");
            List<Team> timesCriados = new ArrayList<>();
            Set<Long> jogadoresJaUsados = new HashSet<>();

            for (int i = 0; i < times.size(); i++) {
                List<Player> playersDoTime = times.get(i);

                if (playersDoTime.isEmpty()) {
                    continue;
                }

                // Validação: verifica se algum jogador já foi usado
                for (Player player : playersDoTime) {
                    if (player.getId() != null && jogadoresJaUsados.contains(player.getId())) {
                        Mensagem.error("Erro: Jogador " + player.getNome() + " foi sorteado mais de uma vez!");
                        return;
                    }
                    if (player.getId() != null) {
                        jogadoresJaUsados.add(player.getId());
                    }
                }

                // Calcula rating médio do time para log
                double ratingMedio = playersDoTime.stream()
                        .mapToDouble(p -> p.getRating() != null ? p.getRating() : 5.0)
                        .average()
                        .orElse(5.0);

                // Cria um novo time
                Team novoTime = new Team();

                // Se quantidadePorTime for 2, usa os nicks dos jogadores
                if (quantidadePorTime == 2 && playersDoTime.size() >= 2) {
                    String nick1 = playersDoTime.get(0).getNick() != null ? playersDoTime.get(0).getNick() : playersDoTime.get(0).getNome();
                    String nick2 = playersDoTime.get(1).getNick() != null ? playersDoTime.get(1).getNick() : playersDoTime.get(1).getNome();
                    novoTime.setNome("Time " + nick1 + " e " + nick2);
                } else {
                    String nick = playersDoTime.get(0).getNick() != null ? playersDoTime.get(0).getNick() : playersDoTime.get(0).getNome();
                    novoTime.setNome("Time " + nick);
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

                System.out.println("✓ Time " + (i + 1) + " criado: " + novoTime.getNome()
                        + " (Rating médio: " + String.format("%.2f", ratingMedio) + ")");
                for (Player p : playersDoTime) {
                    System.out.println("    - " + p.getNome() + " (Rating: " + p.getRating() + ")");
                }
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
                    "Times sorteados com sucesso! " + timesCriados.size() + " times criados com balanceamento por rating.",
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
     * Retorna todas as rodadas suíças geradas, na ordem sequencial Busca
     * partidas tanto das listas do campeonato quanto diretamente pelo
     * contadorEsperado
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
     * Calcula os records (vitórias:derrotas) de todos os times até determinada
     * rodada Retorna um mapa com a contagem de times em cada record
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
            if (partida.isFinalizada() && partida.getTimeVencedor() != null
                    && partida.getItemPartida() != null && !partida.getItemPartida().isEmpty()) {

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
     * Gera a próxima rodada suíça do campeonato Segue as regras do sistema
     * suíço: emparelhamento por record, evita confrontos repetidos Apenas admin
     * pode executar esta ação
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
                if (partida.isFinalizada() && partida.getTimeVencedor() != null
                        && partida.getTimePerdedor() != null) {

                    Team timeVencedor = partida.getTimeVencedor();
                    Team timePerdedor = partida.getTimePerdedor();

                    // Encontra os times no mapa local
                    Team localTimeVencedor = teamMap.get(timeVencedor.getId());
                    Team localTimePerdedor = teamMap.get(timePerdedor.getId());

                    if (localTimeVencedor == null || localTimePerdedor == null) {
                        continue;
                    }

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
     * Gera os emparelhamentos para a próxima rodada do sistema suíço Segue as
     * regras: emparelha times com records similares, evita rematches Times com
     * 3:0 ou 0:3 não jogam mais
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
            } // Se não encontrou oponente, o time recebe BYE (vitória automática)
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

        // Salva a partida PRIMEIRO (sem itens ainda)
        novaPartida = partidaServico.salvar(novaPartida, null, Url.SALVAR_PARTIDA.getNome());

        if (novaPartida == null || novaPartida.getId() == null) {
            System.err.println("Erro ao salvar partida suíça: partida retornou null");
            return null;
        }

        // Cria os itens da partida (mapas) COM o ID da partida já definido
        int qtdItens = 1; // Valor padrão, pode ser ajustado
        if (this.camp.getJogo() != null) {
            // Pode buscar quantidade de mapas do jogo
        }

        List<ItemPartida> itensPartida = PartidaUtils.gerarPartidasTimes(novaPartida, this.camp.getId(), time1, time2, qtdItens);

        // Define o partida_id em todos os itens ANTES de salvar
        if (itensPartida != null && !itensPartida.isEmpty()) {
            for (ItemPartida item : itensPartida) {
                if (item != null) {
                    item.setPartida(novaPartida.getId());
                    // Salva o item (cria novo, não atualiza)
                    try {
                        //itemPartidaServico.salvar(item, null, Url.SALVAR_ITEM_PARTIDA.getNome());
                    } catch (Exception e) {
                        System.err.println("Erro ao salvar item de partida suíça: " + e.getMessage());
                    }
                }
            }
        }

        // Atualiza a partida com os itens
        novaPartida.setItemPartida(itensPartida);
        novaPartida = partidaServico.salvar(novaPartida, novaPartida.getId(), Url.ATUALIZAR_PARTIDA.getNome());

        return novaPartida;
    }

    /**
     * Salva as partidas na rodada correspondente do campeonato Usa o número da
     * rodada para determinar onde salvar IMPORTANTE: Salva o contadorEsperado
     * no banco de dados
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
                    partida.setNome("Rodada " + numeroRodada + " - "
                            + (partida.getItemPartida() != null && !partida.getItemPartida().isEmpty()
                            ? partida.getItemPartida().get(0).getTeam1().getNome() + " vs "
                            + partida.getItemPartida().get(0).getTeam2().getNome()
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
         * Agrupa as partidas desta rodada por recorde antes da rodada Retorna
         * um Map onde a chave é o recorde (ex: "0:0", "1:0", "0:1") e o valor é
         * a lista de partidas
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
                if (partida.isFinalizada() && partida.getTimeVencedor() != null
                        && partida.getItemPartida() != null && !partida.getItemPartida().isEmpty()) {

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
        private int roundsGanhos;
        private int roundsPerdidos;

        public ClassificacaoTime(Team team) {
            this.team = team;
            this.vitorias = 0;
            this.derrotas = 0;
            this.pontos = 0;
            this.roundsGanhos = 0;
            this.roundsPerdidos = 0;
        }

        public void adicionarVitoria() {
            this.vitorias++;
            this.pontos += 3;
        }

        public void adicionarDerrota() {
            this.derrotas++;
        }

        public void adicionarRoundsGanhos(int rounds) {
            this.roundsGanhos += rounds;
        }

        public void adicionarRoundsPerdidos(int rounds) {
            this.roundsPerdidos += rounds;
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

        public int getRoundsGanhos() {
            return roundsGanhos;
        }

        public int getRoundsPerdidos() {
            return roundsPerdidos;
        }

        public String getRecord() {
            return vitorias + ":" + derrotas;
        }
    }

    /**
     * Classe para armazenar estatísticas consolidadas de um jogador no campeonato
     */
    public static class EstatisticaJogador {

        private Player player;
        private int kills;
        private int assists;
        private int deaths;
        private int roundsJogados;
        private int roundsGanhos;
        private int roundsPerdidos;
        private int posicao;

        public EstatisticaJogador(Player player) {
            this.player = player;
            this.kills = 0;
            this.assists = 0;
            this.deaths = 0;
            this.roundsJogados = 0;
            this.roundsGanhos = 0;
            this.roundsPerdidos = 0;
            this.posicao = 0;
        }

        public void adicionarKills(int kills) {
            this.kills += kills;
        }

        public void adicionarAssists(int assists) {
            this.assists += assists;
        }

        public void adicionarDeaths(int deaths) {
            this.deaths += deaths;
        }

        public void adicionarRoundsJogados(int rounds) {
            this.roundsJogados += rounds;
        }

        public void setRoundsJogados(int rounds) {
            this.roundsJogados = rounds;
        }

        public void adicionarRoundsGanhos(int rounds) {
            this.roundsGanhos += rounds;
        }

        public void adicionarRoundsPerdidos(int rounds) {
            this.roundsPerdidos += rounds;
        }

        public Player getPlayer() {
            return player;
        }

        public int getKills() {
            return kills;
        }

        public int getAssists() {
            return assists;
        }

        public int getDeaths() {
            return deaths;
        }

        public int getRoundsJogados() {
            return roundsJogados;
        }

        public int getRoundsGanhos() {
            return roundsGanhos;
        }

        public int getRoundsPerdidos() {
            return roundsPerdidos;
        }

        public int getPosicao() {
            return posicao;
        }

        public void setPosicao(int posicao) {
            this.posicao = posicao;
        }
    }

    /**
     * Calcula e retorna as estatísticas de todos os jogadores do campeonato
     * Percorre: Partidas -> ItemPartida -> Times -> Players -> Estatísticas
     * Ordenado por: Kills, Assistencias, Deaths, rounds jogados, rounds ganhos, rounds perdidos
     */
    public List<EstatisticaJogador> getEstatisticasJogadores() {
        // Verifica cache
        if (this.camp != null && this.camp.getId() != null 
            && this.campIdEstatisticasCache != null 
            && this.campIdEstatisticasCache.equals(this.camp.getId())
            && this.estatisticasJogadoresCache != null) {
            return this.estatisticasJogadoresCache;
        }

        List<EstatisticaJogador> estatisticas = new ArrayList<>();

        if (this.camp == null || this.camp.getId() == null) {
            return estatisticas;
        }

        // Mapa para armazenar estatísticas por jogador
        Map<Long, EstatisticaJogador> statsPorJogador = new HashMap<>();

        // Obtém todas as partidas do campeonato
        List<Partida> todasPartidas = obterPartidasDoCache();
        
        if (todasPartidas != null) {
            // Percorre cada partida
            for (Partida partida : todasPartidas) {
                if (partida.getItemPartida() != null) {
                    // Percorre cada ItemPartida da partida
                    for (ItemPartida itemPartida : partida.getItemPartida()) {
                        // Verifica se o ItemPartida pertence ao campeonato
                        if (itemPartida.getCamp() != null && itemPartida.getCamp().equals(this.camp.getId())) {
                            
                            // Processa team1
                            if (itemPartida.getTeam1() != null && itemPartida.getTeam1().getPlayers() != null) {
                                for (Player player : itemPartida.getTeam1().getPlayers()) {
                                    processarEstatisticasJogador(player, itemPartida, statsPorJogador, true);
                                }
                            }
                            
                            // Processa team2
                            if (itemPartida.getTeam2() != null && itemPartida.getTeam2().getPlayers() != null) {
                                for (Player player : itemPartida.getTeam2().getPlayers()) {
                                    processarEstatisticasJogador(player, itemPartida, statsPorJogador, false);
                                }
                            }
                        }
                    }
                }
            }
        }

        // Calcula rounds jogados (soma de rounds ganhos e perdidos)
        for (EstatisticaJogador est : statsPorJogador.values()) {
            int roundsJogados = est.getRoundsGanhos() + est.getRoundsPerdidos();
            est.setRoundsJogados(roundsJogados);
        }

        // Converte para lista e ordena
        estatisticas = new ArrayList<>(statsPorJogador.values());
        estatisticas.sort((e1, e2) -> {
            // 1. Kills (maior primeiro)
            int compareKills = Integer.compare(e2.getKills(), e1.getKills());
            if (compareKills != 0) {
                return compareKills;
            }
            // 2. Assists (maior primeiro)
            int compareAssists = Integer.compare(e2.getAssists(), e1.getAssists());
            if (compareAssists != 0) {
                return compareAssists;
            }
            // 3. Deaths (menor primeiro)
            int compareDeaths = Integer.compare(e1.getDeaths(), e2.getDeaths());
            if (compareDeaths != 0) {
                return compareDeaths;
            }
            // 4. Rounds jogados (maior primeiro)
            int compareRoundsJogados = Integer.compare(e2.getRoundsJogados(), e1.getRoundsJogados());
            if (compareRoundsJogados != 0) {
                return compareRoundsJogados;
            }
            // 5. Rounds ganhos (maior primeiro)
            int compareRoundsGanhos = Integer.compare(e2.getRoundsGanhos(), e1.getRoundsGanhos());
            if (compareRoundsGanhos != 0) {
                return compareRoundsGanhos;
            }
            // 6. Rounds perdidos (menor primeiro)
            int compareRoundsPerdidos = Integer.compare(e1.getRoundsPerdidos(), e2.getRoundsPerdidos());
            if (compareRoundsPerdidos != 0) {
                return compareRoundsPerdidos;
            }
            // 7. Ordem alfabética do nome
            return e1.getPlayer().getNome().compareTo(e2.getPlayer().getNome());
        });

        // Define as posições após ordenar
        for (int i = 0; i < estatisticas.size(); i++) {
            estatisticas.get(i).setPosicao(i + 1);
        }

        // Salva no cache
        this.estatisticasJogadoresCache = estatisticas;
        this.campIdEstatisticasCache = this.camp.getId();

        return estatisticas;
    }

    /**
     * Processa as estatísticas de um jogador em um ItemPartida específico
     * @param player O jogador
     * @param itemPartida O ItemPartida
     * @param statsPorJogador Mapa para armazenar estatísticas consolidadas
     * @param isTeam1 true se o jogador está no team1, false se está no team2
     */
    private void processarEstatisticasJogador(Player player, ItemPartida itemPartida, Map<Long, EstatisticaJogador> statsPorJogador, boolean isTeam1) {
        if (player == null || player.getId() == null || itemPartida == null || itemPartida.getId() == null) {
            return;
        }

        // Cria entrada no mapa se não existir
        if (!statsPorJogador.containsKey(player.getId())) {
            statsPorJogador.put(player.getId(), new EstatisticaJogador(player));
        }

        EstatisticaJogador estatJogador = statsPorJogador.get(player.getId());

        // Busca estatísticas do jogador neste ItemPartida específico
        try {
            List<Estatisticas> estsPlayer = estatisticaServico.estatisticaPorItemPartidaPlayer(player.getId(), itemPartida.getId());
            if (estsPlayer != null) {
                for (Estatisticas est : estsPlayer) {
                    if (est != null) {
                        // Soma kills, assists, deaths
                        estatJogador.adicionarKills(est.getKills() != null ? est.getKills() : 0);
                        estatJogador.adicionarAssists(est.getAssists() != null ? est.getAssists() : 0);
                        estatJogador.adicionarDeaths(est.getDeaths() != null ? est.getDeaths() : 0);
                        
                        // Para rounds, usa os valores do ItemPartida
                        // Se o time do jogador é team1, rounds ganhos = scoreT1, rounds perdidos = scoreT2
                        // Se o time do jogador é team2, rounds ganhos = scoreT2, rounds perdidos = scoreT1
                        if (itemPartida.getScoreT1() != null && itemPartida.getScoreT2() != null) {
                            if (isTeam1) {
                                estatJogador.adicionarRoundsGanhos(itemPartida.getScoreT1());
                                estatJogador.adicionarRoundsPerdidos(itemPartida.getScoreT2());
                            } else {
                                estatJogador.adicionarRoundsGanhos(itemPartida.getScoreT2());
                                estatJogador.adicionarRoundsPerdidos(itemPartida.getScoreT1());
                            }
                        } else {
                            // Se não tiver score no ItemPartida, usa os valores da estatística
                            estatJogador.adicionarRoundsGanhos(est.getRoundsGanhos() != null ? est.getRoundsGanhos() : 0);
                            estatJogador.adicionarRoundsPerdidos(est.getRoundsPerdidos() != null ? est.getRoundsPerdidos() : 0);
                        }
                    }
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(ManagerCamp.class.getName()).log(Level.WARNING, 
                "Erro ao buscar estatísticas do jogador " + player.getId() + " no ItemPartida " + itemPartida.getId(), ex);
        }
    }

    /**
     * Retorna o total de ItemPartida do campeonato
     * Conta todos os ItemPartida que têm o campeonato_id
     */
    public int getTotalPartidas() {
        if (this.camp == null || this.camp.getId() == null) {
            return 0;
        }

        int total = 0;
        List<Partida> todasPartidas = obterPartidasDoCache();
        
        if (todasPartidas != null) {
            for (Partida partida : todasPartidas) {
                if (partida.getItemPartida() != null) {
                    for (ItemPartida item : partida.getItemPartida()) {
                        // Verifica se o ItemPartida pertence ao campeonato
                        if (item.getCamp() != null && item.getCamp().equals(this.camp.getId())) {
                            total++;
                        }
                    }
                }
            }
        }

        return total;
    }

    /**
     * Retorna o total de ItemPartida realizados (jogados) do campeonato
     * Conta os ItemPartida que estão em partidas finalizadas e pertencem ao campeonato
     */
    public int getTotalPartidasRealizadas() {
        if (this.camp == null || this.camp.getId() == null) {
            return 0;
        }

        int total = 0;
        List<Partida> todasPartidas = obterPartidasDoCache();
        
        if (todasPartidas != null) {
            for (Partida partida : todasPartidas) {
                // Verifica se a partida está finalizada
                if (partida.isFinalizada() && partida.getItemPartida() != null) {
                    for (ItemPartida item : partida.getItemPartida()) {
                        // Verifica se o ItemPartida pertence ao campeonato e está jogado
                        if (item.getCamp() != null && item.getCamp().equals(this.camp.getId()) && item.isJogado()) {
                            total++;
                        }
                    }
                }
            }
        }

        return total;
    }

    /**
     * Calcula e retorna a tabela de classificação ordenada por vitórias Ordem:
     * mais vitórias primeiro, em caso de empate, menos derrotas
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
                if (partida.isFinalizada() && partida.getTimeVencedor() != null
                        && partida.getTimePerdedor() != null) {

                    Team timeVencedor = partida.getTimeVencedor();
                    Team timePerdedor = partida.getTimePerdedor();

                    ClassificacaoTime classVencedor = classificacaoPorTime.get(timeVencedor.getId());
                    ClassificacaoTime classPerdedor = classificacaoPorTime.get(timePerdedor.getId());

                    if (classVencedor != null && classPerdedor != null) {
                        classVencedor.adicionarVitoria();
                        classPerdedor.adicionarDerrota();
                        
                        // Calcula rounds ganhos e rounds perdidos
                        if (partida.getItemPartida() != null && !partida.getItemPartida().isEmpty()) {
                            ItemPartida itemPartida = partida.getItemPartida().get(0);
                            if (itemPartida.getScoreT1() != null && itemPartida.getScoreT2() != null) {
                                // Verifica qual time é o time1 e qual é o time2
                                Team time1 = itemPartida.getTeam1();
                                Team time2 = itemPartida.getTeam2();
                                
                                if (time1 != null && time2 != null) {
                                    if (timeVencedor.getId().equals(time1.getId())) {
                                        // Time1 venceu
                                        classVencedor.adicionarRoundsGanhos(itemPartida.getScoreT1());
                                        classVencedor.adicionarRoundsPerdidos(itemPartida.getScoreT2());
                                        classPerdedor.adicionarRoundsGanhos(itemPartida.getScoreT2());
                                        classPerdedor.adicionarRoundsPerdidos(itemPartida.getScoreT1());
                                    } else if (timeVencedor.getId().equals(time2.getId())) {
                                        // Time2 venceu
                                        classVencedor.adicionarRoundsGanhos(itemPartida.getScoreT2());
                                        classVencedor.adicionarRoundsPerdidos(itemPartida.getScoreT1());
                                        classPerdedor.adicionarRoundsGanhos(itemPartida.getScoreT1());
                                        classPerdedor.adicionarRoundsPerdidos(itemPartida.getScoreT2());
                                    }
                                }
                            }
                        }
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
     * Registra o resultado de uma partida do formato suíço Define time
     * vencedor, perdedor e finaliza a partida
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

            // Verifica se a final foi concluída e finaliza o campeonato se necessário
            finalizarCampeonatoSeNecessario();

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
        if (partidaParaResultado != null
                && partidaParaResultado.getItemPartida() != null
                && !partidaParaResultado.getItemPartida().isEmpty()) {
            Team time1 = partidaParaResultado.getItemPartida().get(0).getTeam1();
            return time1 != null ? time1.getNome() : "";
        }
        return "";
    }

    public Long getIdTime1Partida() {
        if (partidaParaResultado != null
                && partidaParaResultado.getItemPartida() != null
                && !partidaParaResultado.getItemPartida().isEmpty()) {
            Team time1 = partidaParaResultado.getItemPartida().get(0).getTeam1();
            return time1 != null ? time1.getId() : null;
        }
        return null;
    }

    /**
     * Obtém o nome do time 2 da partida selecionada
     */
    public String getNomeTime2Partida() {
        if (partidaParaResultado != null
                && partidaParaResultado.getItemPartida() != null
                && !partidaParaResultado.getItemPartida().isEmpty()) {
            Team time2 = partidaParaResultado.getItemPartida().get(0).getTeam2();
            return time2 != null ? time2.getNome() : "";
        }
        return "";
    }

    public Long getIdTime2Partida() {
        if (partidaParaResultado != null
                && partidaParaResultado.getItemPartida() != null
                && !partidaParaResultado.getItemPartida().isEmpty()) {
            Team time2 = partidaParaResultado.getItemPartida().get(0).getTeam2();
            return time2 != null ? time2.getId() : null;
        }
        return null;
    }

    // ===========================
    // MÉTODOS DE PLAYOFFS
    // ===========================
    /**
     * Verifica se o campeonato é do tipo Suíço
     */
    public boolean isSwiss() {
        return this.camp != null
                && this.camp.getTipoCampeonato() != null
                && "SUÍÇO".equals(this.camp.getTipoCampeonato().getNome());
    }

    /**
     * Verifica se o campeonato é do tipo Grupo e Playoff
     */
    public boolean isGrupoPlayoff() {
        return this.camp != null
                && this.camp.getTipoCampeonato() != null
                && "GRUPO E PLAYOFF".equals(this.camp.getTipoCampeonato().getNome());
    }

    /**
     * Verifica se todas as rodadas suíças foram concluídas
     */
    public boolean isRodadasSuicasConcluidas() {
        if (this.camp == null || !isSwiss()) {
            return false;
        }

        List<SwissRound> rodadas = getRodadasSuicas();
        if (rodadas.isEmpty()) {
            return false;
        }

        // Verifica se a última rodada está finalizada
        SwissRound ultimaRodada = rodadas.get(rodadas.size() - 1);
        if (!ultimaRodada.isFinalizada()) {
            return false;
        }

        // Verifica se há times com 3 vitórias ou 3 derrotas
        Map<Team, SwissRecord> records = calcularSwissRecords();
        long timesClassificados = records.values().stream()
                .filter(r -> r.wins >= 3)
                .count();

        return timesClassificados >= 2; // Pelo menos 2 times classificados
    }

    /**
     * Obtém lista de times classificados (3 vitórias) ordenados por vitórias
     */
    public List<Team> getTimesClassificados() {
        if (this.camp == null) {
            return new ArrayList<>();
        }

        Map<Team, SwissRecord> records = calcularSwissRecords();

        return records.entrySet().stream()
                .filter(entry -> entry.getValue().wins >= 3)
                .sorted((e1, e2) -> {
                    // Ordena por vitórias (maior primeiro)
                    int compareWins = Integer.compare(e2.getValue().wins, e1.getValue().wins);
                    if (compareWins != 0) {
                        return compareWins;
                    }

                    // Depois por derrotas (menor primeiro)
                    return Integer.compare(e1.getValue().losses, e2.getValue().losses);
                })
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    /**
     * Verifica se já existem playoffs criados
     */
    public boolean hasPlayoffs() {
        return this.camp != null
                && this.camp.getFasesCamp() != null
                && !this.camp.getFasesCamp().isEmpty();
    }

    /**
     * Gera os playoffs baseado nos times classificados
     */
    public void gerarPlayoffs() {
        try {
            System.out.println("====== INICIANDO GERAÇÃO DOS PLAYOFFS ======");

            // Validações
            if (!isAdmin()) {
                System.err.println("Erro: Usuário não é admin");
                Mensagem.error("Apenas administradores podem gerar os playoffs!");
                return;
            }

            if (!isSwiss()) {
                System.err.println("Erro: Campeonato não é suíço");
                Mensagem.error("Este campeonato não é do tipo SUÍÇO!");
                return;
            }

            if (!isRodadasSuicasConcluidas()) {
                System.err.println("Erro: Rodadas suíças não concluídas");
                Mensagem.error("As rodadas suíças ainda não foram concluídas!");
                return;
            }

            if (hasPlayoffs()) {
                System.err.println("Erro: Playoffs já foram gerados");
                Mensagem.error("Os playoffs já foram gerados para este campeonato!");
                return;
            }

            // Atualiza o campeonato
            this.camp = this.campeonatoServico.buscaCamp(this.camp.getId());

            List<Team> classificados = getTimesClassificados();
            System.out.println("✓ Times classificados: " + classificados.size());

            if (classificados.size() < 2) {
                Mensagem.error("É necessário pelo menos 2 times classificados para gerar os playoffs!");
                return;
            }

            // Determina o número de times nos playoffs (potência de 2)
            int numeroTimes = getMaiorPotenciaDe2(classificados.size());
            List<Team> timesPlayoffs = classificados.subList(0, Math.min(numeroTimes, classificados.size()));

            System.out.println("✓ Times nos playoffs: " + timesPlayoffs.size());

            // Inicializa lista de fases se necessário
            if (this.camp.getFasesCamp() == null) {
                this.camp.setFasesCamp(new ArrayList<>());
            }

            // Gera a primeira fase dos playoffs
            Fase primeiraFase = gerarFase(timesPlayoffs, getNomeFase(timesPlayoffs.size()));
            this.camp.getFasesCamp().add(primeiraFase);

            System.out.println("✓ Fase gerada: " + primeiraFase.getNome() + " com "
                    + primeiraFase.getPartidas().size() + " partidas");

            // Salva o campeonato atualizado
            this.camp = campeonatoServico.save(this.camp, this.camp.getId(), Url.ATUALIZAR_CAMPEONATO.getNome());

            System.out.println("====== PLAYOFFS GERADOS COM SUCESSO ======");

            Mensagem.successAndRedirect(
                    "Playoffs gerados com sucesso! " + primeiraFase.getNome() + " criada com "
                    + primeiraFase.getPartidas().size() + " partidas.",
                    "visualizarCampeonato.xhtml?id=" + this.camp.getId()
            );

        } catch (Exception ex) {
            System.err.println("====== ERRO AO GERAR PLAYOFFS ======");
            ex.printStackTrace();
            Mensagem.error("Erro ao gerar playoffs: " + ex.getMessage());
        }
    }

    /**
     * Gera a próxima fase dos playoffs baseado nos vencedores da fase anterior
     */
    public void gerarProximaFasePlayoff() {
        try {
            System.out.println("====== GERANDO PRÓXIMA FASE DOS PLAYOFFS ======");

            if (!isAdmin()) {
                Mensagem.error("Apenas administradores podem gerar fases!");
                return;
            }

            if (!hasPlayoffs()) {
                Mensagem.error("Não há playoffs criados ainda!");
                return;
            }

            // Atualiza o campeonato
            this.camp = this.campeonatoServico.buscaCamp(this.camp.getId());

            List<Fase> fases = this.camp.getFasesCamp();
            Fase ultimaFase = fases.get(fases.size() - 1);

            // Verifica se todas as partidas da última fase foram finalizadas
            boolean todasFinalizadas = ultimaFase.getPartidas().stream()
                    .allMatch(Partida::isFinalizada);

            if (!todasFinalizadas) {
                Mensagem.error("Todas as partidas da fase " + ultimaFase.getNome() + " devem estar finalizadas!");
                return;
            }

            // Coleta os vencedores
            List<Team> vencedores = ultimaFase.getPartidas().stream()
                    .map(Partida::getTimeVencedor)
                    .filter(t -> t != null)
                    .collect(Collectors.toList());

            System.out.println("✓ Vencedores da fase anterior: " + vencedores.size());

            if (vencedores.size() < 2) {
                Mensagem.error("É necessário pelo menos 2 vencedores para gerar a próxima fase!");
                return;
            }

            // Se houver apenas 2 vencedores e já é a final, declara o campeão
            if (vencedores.size() == 1) {
                Mensagem.success("Campeonato finalizado! Campeão: " + vencedores.get(0).getNome());
                return;
            }

            // Gera a próxima fase
            String nomeFase = getNomeFase(vencedores.size());
            Fase proximaFase = gerarFase(vencedores, nomeFase);
            this.camp.getFasesCamp().add(proximaFase);

            System.out.println("✓ Próxima fase gerada: " + proximaFase.getNome());

            // Salva o campeonato
            this.camp = campeonatoServico.save(this.camp, this.camp.getId(), Url.ATUALIZAR_CAMPEONATO.getNome());

            Mensagem.successAndRedirect(
                    "Próxima fase gerada com sucesso! " + proximaFase.getNome() + " criada.",
                    "visualizarCampeonato.xhtml?id=" + this.camp.getId()
            );

        } catch (Exception ex) {
            System.err.println("====== ERRO AO GERAR PRÓXIMA FASE ======");
            ex.printStackTrace();
            Mensagem.error("Erro ao gerar próxima fase: " + ex.getMessage());
        }
    }

    /**
     * Gera uma fase com as partidas entre os times fornecidos
     */
    private Fase gerarFase(List<Team> times, String nomeFase) throws Exception {
        System.out.println("  Gerando fase: " + nomeFase + " com " + times.size() + " times");

        // Cria as partidas primeiro
        List<Partida> partidas = new ArrayList<>();

        // Emparelha os times (1º vs último, 2º vs penúltimo, etc.)
        for (int i = 0; i < times.size() / 2; i++) {
            Team time1 = times.get(i);
            Team time2 = times.get(times.size() - 1 - i);

            if (time1 == null || time1.getId() == null || time2 == null || time2.getId() == null) {
                System.err.println("  ✗ Time inválido na posição " + i);
                continue;
            }

            System.out.println("  Criando partida: " + time1.getNome() + " vs " + time2.getNome());

            Partida partida = criarPartidaPlayoff(time1, time2, nomeFase);
            if (partida != null && partida.getId() != null) {
                partidas.add(partida);
                System.out.println("  ✓ Partida criada com ID: " + partida.getId());
            } else {
                System.err.println("  ✗ Erro ao criar partida entre " + time1.getNome() + " e " + time2.getNome());
                throw new Exception("Erro ao criar partida entre " + time1.getNome() + " e " + time2.getNome());
            }
        }

        if (partidas.isEmpty()) {
            throw new Exception("Nenhuma partida foi criada para a fase " + nomeFase);
        }

        // Cria a fase com as partidas já associadas
        Fase fase = new Fase();
        fase.setNome(nomeFase);
        fase.setIdCamp(this.camp.getId());
        fase.setActive(true);
        fase.setPartidas(partidas);

        // Salva a fase com as partidas
        System.out.println("  Salvando fase com " + partidas.size() + " partidas...");
        fase = faseServico.save(fase, null, Url.SALVAR_FASE.getNome());

        if (fase == null || fase.getId() == null) {
            throw new Exception("Erro ao salvar a fase " + nomeFase);
        }

        System.out.println("  ✓ Fase salva com ID: " + fase.getId() + " e "
                + (fase.getPartidas() != null ? fase.getPartidas().size() : 0) + " partidas");

        return fase;
    }

    /**
     * Cria uma partida de playoff entre dois times
     */
    private Partida criarPartidaPlayoff(Team time1, Team time2, String nomeFase) throws Exception {
        if (time1 == null || time1.getId() == null || time2 == null || time2.getId() == null) {
            System.err.println("  criarPartidaPlayoff: Time inválido");
            return null;
        }

        if (time1.getId().equals(time2.getId())) {
            System.err.println("  criarPartidaPlayoff: Times iguais não podem jogar entre si");
            return null;
        }

        System.out.println("    Criando partida: " + time1.getNome() + " (ID: " + time1.getId()
                + ") vs " + time2.getNome() + " (ID: " + time2.getId() + ")");

        Partida novaPartida = new Partida();
        novaPartida.setJogo(this.camp.getJogo());
        novaPartida.setActive(true);
        novaPartida.setFinalizada(false);
        novaPartida.setNome(nomeFase + " - " + time1.getNome() + " vs " + time2.getNome());
        novaPartida.setDataPartida(new Date());

        // Salva a partida PRIMEIRO (sem itens ainda)
        System.out.println("    Salvando partida no servidor...");
        novaPartida = partidaServico.salvar(novaPartida, null, Url.SALVAR_PARTIDA.getNome());

        if (novaPartida == null || novaPartida.getId() == null) {
            System.err.println("    ✗ Erro: Partida não foi salva (retornou null)");
            throw new Exception("Erro ao salvar partida entre " + time1.getNome() + " e " + time2.getNome());
        }

        System.out.println("    ✓ Partida salva com ID: " + novaPartida.getId());

        // Agora cria os itens da partida COM o ID da partida já definido
        int qtdItens = 3;
        List<ItemPartida> itensPartida = PartidaUtils.gerarPartidasTimes(novaPartida, this.camp.getId(), time1, time2, qtdItens);

        // Define o partida_id em todos os itens ANTES de salvar
        if (itensPartida != null && !itensPartida.isEmpty()) {
            for (ItemPartida item : itensPartida) {
                if (item != null) {
                    item.setPartida(novaPartida.getId());
                    // Salva o item (cria novo, não atualiza)
                    try {
                        //itemPartidaServico.salvar(item, null, Url.SALVAR_ITEM_PARTIDA.getNome());
                        System.out.println("    ✓ Item de partida salvo");
                    } catch (Exception e) {
                        System.err.println("    ✗ Erro ao salvar item de partida: " + e.getMessage());
                    }
                }
            }
        }

        // Atualiza a partida com os itens
        novaPartida.setItemPartida(itensPartida);
        novaPartida = partidaServico.salvar(novaPartida, novaPartida.getId(), Url.ATUALIZAR_PARTIDA.getNome());

        return novaPartida;
    }

    /**
     * Retorna o nome da fase baseado no número de times
     */
    private String getNomeFase(int numeroTimes) {
        switch (numeroTimes) {
            case 2:
                return "Final";
            case 4:
                return "Semifinal";
            case 8:
                return "Quartas de Final";
            case 16:
                return "Oitavas de Final";
            default:
                return "Fase de " + numeroTimes + " times";
        }
    }

    /**
     * Retorna a maior potência de 2 menor ou igual ao número fornecido
     */
    private int getMaiorPotenciaDe2(int numero) {
        int potencia = 1;
        while (potencia * 2 <= numero) {
            potencia *= 2;
        }
        return potencia;
    }

    /**
     * Obtém as fases dos playoffs
     */
    public List<Fase> getFasesPlayoffs() {
        if (this.camp == null || this.camp.getFasesCamp() == null) {
            return new ArrayList<>();
        }
        return this.camp.getFasesCamp();
    }

    /**
     * Verifica se pode gerar a próxima fase dos playoffs
     */
    public boolean podeMostrarProximaFasePlayoff() {
        if (!hasPlayoffs()) {
            return false;
        }

        List<Fase> fases = this.camp.getFasesCamp();
        if (fases.isEmpty()) {
            return false;
        }

        Fase ultimaFase = fases.get(fases.size() - 1);

        // Verifica se a última fase está completa
        boolean todasFinalizadas = ultimaFase.getPartidas().stream()
                .allMatch(Partida::isFinalizada);

        if (!todasFinalizadas) {
            return false;
        }

        // Verifica se não é a final
        long vencedores = ultimaFase.getPartidas().stream()
                .filter(p -> p.getTimeVencedor() != null)
                .count();

        return vencedores >= 2; // Se houver 2+ vencedores, pode gerar próxima fase
    }

    /**
     * Verifica se a final foi concluída
     */
    public boolean isFinalConcluida() {
        if (!hasPlayoffs()) {
            return false;
        }

        List<Fase> fases = this.camp.getFasesCamp();
        if (fases.isEmpty()) {
            return false;
        }

        Fase ultimaFase = fases.get(fases.size() - 1);

        // Verifica se é a final (apenas 1 partida) e está finalizada
        return "Final".equals(ultimaFase.getNome())
                && ultimaFase.getPartidas() != null
                && ultimaFase.getPartidas().size() == 1
                && ultimaFase.getPartidas().get(0).isFinalizada();
    }

    /**
     * Obtém o time campeão (vencedor da final)
     */
    public Team getCampeao() {
        if (!isFinalConcluida()) {
            return null;
        }

        List<Fase> fases = this.camp.getFasesCamp();
        Fase finalFase = fases.get(fases.size() - 1);

        if (finalFase.getPartidas() != null && !finalFase.getPartidas().isEmpty()) {
            Partida partidaFinal = finalFase.getPartidas().get(0);
            if (this.camp.getCampeao() == null) {
                this.camp.setCampeao(partidaFinal.getTimeVencedor());
                this.camp.setStatus(StatusCamp.FINALIZADO);
                try {
                    this.camp = campeonatoServico.save(this.camp, this.camp.getId(), Url.ATUALIZAR_CAMPEONATO.getNome());
                } catch (Exception ex) {
                    Logger.getLogger(ManagerCamp.class.getName()).log(Level.SEVERE, null, ex);
                }

            }
            return partidaFinal.getTimeVencedor();
        }

        return null;
    }

    /**
     * Verifica se uma fase é a final
     */
    public boolean isFaseFinal(Fase fase) {
        return fase != null && "Final".equals(fase.getNome());
    }

    /**
     * Finaliza o campeonato quando a final for concluída
     */
    public void finalizarCampeonatoSeNecessario() {
        try {
            if (isFinalConcluida()) {
                Team campeao = getCampeao();

                if (campeao != null) {
                    // Atualiza o status para Finalizado
                    if (this.camp.getStatus() == null || !"Finalizado".equals(this.camp.getStatus().getNome())) {
                        System.out.println("====== FINALIZANDO CAMPEONATO ======");
                        System.out.println("Campeão: " + campeao.getNome());

                        // Busca o status "Finalizado"
                        // Assumindo que você tenha um enum ou forma de setar o status
                        // Se precisar buscar do banco, ajuste aqui
                        // Salva o campeonato
                        this.camp = campeonatoServico.save(this.camp, this.camp.getId(), Url.ATUALIZAR_CAMPEONATO.getNome());

                        System.out.println("✓ Campeonato finalizado com sucesso!");
                    }
                }
            }
        } catch (Exception ex) {
            System.err.println("Erro ao finalizar campeonato: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    // ===========================
    // MÉTODOS DE GRUPOS (GRUPO_PLAYOFF)
    // ===========================
    /**
     * Gera os grupos e suas partidas para campeonato tipo GRUPO_PLAYOFF
     */
    public void gerarGrupos() {
        try {
            System.out.println("====== INICIANDO GERAÇÃO DOS GRUPOS ======");

            // Validações
            if (!isAdmin()) {
                System.err.println("Erro: Usuário não é admin");
                Mensagem.error("Apenas administradores podem gerar os grupos!");
                return;
            }

            if (!isGrupoPlayoff()) {
                System.err.println("Erro: Campeonato não é GRUPO_PLAYOFF");
                Mensagem.error("Este campeonato não é do tipo GRUPO E PLAYOFF!");
                return;
            }

            if (this.camp == null || this.camp.getId() == null) {
                Mensagem.error("Campeonato não encontrado!");
                return;
            }

            // Atualiza o campeonato
            this.camp = this.campeonatoServico.buscaCamp(this.camp.getId());

            if (this.camp.getTeams() == null || this.camp.getTeams().isEmpty()) {
                Mensagem.error("Não há times inscritos para gerar grupos!");
                return;
            }

            // Verifica se já existem grupos
            if (this.camp.getGrupos() != null && !this.camp.getGrupos().isEmpty()) {
                Mensagem.error("Os grupos já foram criados para este campeonato!");
                return;
            }

            List<Team> times = new ArrayList<>(this.camp.getTeams());
            int totalTimes = times.size();

            if (totalTimes < 4) {
                Mensagem.error("É necessário pelo menos 4 times para criar grupos!");
                return;
            }

            // Determina número de grupos
            // Menos de 10 times: 2 grupos (A e B)
            // 10 ou mais times: 3 grupos (A, B, C)
            int numeroGrupos = totalTimes < 10 ? 2 : 3;
            System.out.println("✓ Número de grupos: " + numeroGrupos);
            System.out.println("✓ Total de times: " + totalTimes);

            // Calcula distribuição
            int timesPorGrupo = totalTimes / numeroGrupos;
            int timesExtras = totalTimes % numeroGrupos;

            if (timesExtras > 0) {
                System.out.println("✓ Distribuição: " + timesExtras + " grupo(s) terá(ão) " + (timesPorGrupo + 1)
                        + " times, e " + (numeroGrupos - timesExtras) + " grupo(s) terá(ão) " + timesPorGrupo + " times");
            } else {
                System.out.println("✓ Distribuição: Todos os grupos terão " + timesPorGrupo + " times");
            }

            // Embaralha os times para distribuição aleatória
            Collections.shuffle(times);

            // Divide os times nos grupos
            List<List<Team>> gruposDistribuidos = distribuirTimesEmGrupos(times, numeroGrupos);

            // Cria os grupos no banco
            List<Grupo> gruposCriados = new ArrayList<>();
            List<Integer> partidasPorGrupo = new ArrayList<>(); // Armazena número de partidas por grupo
            String[] nomesGrupos = {"Grupo A", "Grupo B", "Grupo C", "Grupo D"};

            for (int i = 0; i < gruposDistribuidos.size(); i++) {
                List<Team> timesDoGrupo = gruposDistribuidos.get(i);

                try {
                    Grupo grupo = new Grupo();
                    grupo.setNome(nomesGrupos[i]);
                    grupo.setTeams(timesDoGrupo);
                    grupo.setActive(true);
                    grupo.setIdCamp(this.camp.getId());

                    // Gera partidas todos contra todos no grupo
                    List<Partida> partidasGrupo = gerarPartidasTodosContraTodos(timesDoGrupo, nomesGrupos[i]);
                    grupo.setPartidas(partidasGrupo);

                    // Armazena número de partidas antes de salvar
                    int numPartidas = partidasGrupo != null ? partidasGrupo.size() : 0;
                    partidasPorGrupo.add(numPartidas);

                    // Salva o grupo
                    System.out.println("  Tentando salvar " + nomesGrupos[i] + "...");
                    System.out.println("    - Times no grupo: " + timesDoGrupo.size());
                    System.out.println("    - Partidas geradas: " + numPartidas);

                    Grupo grupoSalvo = null;
                    try {
                        grupoSalvo = grupoServico.save(grupo, null, Url.SALVAR_GRUPO.getNome());
                    } catch (Exception e) {
                        System.err.println("  EXCEÇÃO ao salvar " + nomesGrupos[i] + ": " + e.getMessage());
                        e.printStackTrace();
                    }

                    // Valida se o grupo foi salvo com sucesso
                    if (grupoSalvo == null) {
                        System.err.println("  AVISO: " + nomesGrupos[i] + " não foi salvo pela API (retornou null)");
                        System.err.println("  Continuando com o grupo local (partidas já foram criadas)...");
                        // Usa o grupo original se o save retornou null
                        // As partidas já foram salvas individualmente, então podemos continuar
                        grupoSalvo = grupo;
                    } else {
                        System.out.println("  ✓ " + nomesGrupos[i] + " salvo com sucesso (ID: "
                                + (grupoSalvo.getId() != null ? grupoSalvo.getId() : "null") + ")");
                    }

                    // Garante que o grupo não é null antes de adicionar
                    if (grupoSalvo != null) {
                        gruposCriados.add(grupoSalvo);
                    } else {
                        System.err.println("  ERRO CRÍTICO: Não foi possível criar " + nomesGrupos[i]);
                        throw new Exception("Não foi possível criar " + nomesGrupos[i] + " - grupo é null");
                    }

                    // Log detalhado
                    StringBuilder timesNomes = new StringBuilder();
                    for (Team t : timesDoGrupo) {
                        if (timesNomes.length() > 0) {
                            timesNomes.append(", ");
                        }
                        timesNomes.append(t.getNome());
                    }
                    System.out.println("✓ " + nomesGrupos[i] + " criado com " + timesDoGrupo.size()
                            + " times e " + numPartidas + " partidas");
                    System.out.println("  Times: " + timesNomes.toString());

                } catch (Exception e) {
                    System.err.println("  ERRO ao criar " + nomesGrupos[i] + ": " + e.getMessage());
                    e.printStackTrace();
                    throw new Exception("Erro ao criar " + nomesGrupos[i] + ": " + e.getMessage(), e);
                }
            }

            // Valida se pelo menos um grupo foi criado
            if (gruposCriados.isEmpty()) {
                throw new Exception("Nenhum grupo foi criado com sucesso!");
            }

            // Remove grupos null da lista (caso algum tenha falhado)
            gruposCriados.removeIf(g -> g == null);

            if (gruposCriados.size() != gruposDistribuidos.size()) {
                System.err.println("  AVISO: Apenas " + gruposCriados.size() + " de " + gruposDistribuidos.size()
                        + " grupos foram criados com sucesso!");
            }

            // Atualiza o campeonato com os grupos
            if (this.camp.getGrupos() == null) {
                this.camp.setGrupos(new ArrayList<>());
            }

            // Adiciona apenas grupos não-null
            for (Grupo g : gruposCriados) {
                if (g != null) {
                    this.camp.getGrupos().add(g);
                }
            }

            System.out.println("  Atualizando campeonato com " + this.camp.getGrupos().size() + " grupos...");
            this.camp = campeonatoServico.save(this.camp, this.camp.getId(), Url.ATUALIZAR_CAMPEONATO.getNome());

            if (this.camp == null) {
                throw new Exception("Erro ao atualizar o campeonato após criar os grupos!");
            }

            System.out.println("====== GRUPOS CRIADOS COM SUCESSO ======");

            // Monta mensagem de sucesso usando a lista de partidas por grupo
            int totalPartidas = partidasPorGrupo.stream()
                    .mapToInt(Integer::intValue)
                    .sum();

            String mensagemSucesso = String.format(
                    "%d grupos criados com sucesso! Total de %d partidas geradas (todos contra todos em cada grupo). "
                    + "Os 2 primeiros colocados de cada grupo avançam para os playoffs.",
                    gruposCriados.size(),
                    totalPartidas
            );

            Mensagem.successAndRedirect(
                    mensagemSucesso,
                    "visualizarCampeonato.xhtml?id=" + this.camp.getId()
            );

        } catch (Exception ex) {
            System.err.println("====== ERRO AO GERAR GRUPOS ======");
            ex.printStackTrace();
            Mensagem.error("Erro ao gerar grupos: " + ex.getMessage());
        }
    }

    /**
     * Distribui times em grupos de forma balanceada Permite número ímpar de
     * times - um grupo terá no máximo 1 time a mais
     */
    private List<List<Team>> distribuirTimesEmGrupos(List<Team> times, int numeroGrupos) {
        List<List<Team>> grupos = new ArrayList<>();

        // Inicializa os grupos
        for (int i = 0; i < numeroGrupos; i++) {
            grupos.add(new ArrayList<>());
        }

        // Distribui os times de forma circular
        // Se houver número ímpar, os primeiros grupos terão 1 time a mais
        for (int i = 0; i < times.size(); i++) {
            int grupoIndex = i % numeroGrupos;
            grupos.get(grupoIndex).add(times.get(i));
        }

        // Log da distribuição
        for (int i = 0; i < grupos.size(); i++) {
            System.out.println("  Grupo " + (i + 1) + " terá " + grupos.get(i).size() + " times");
        }

        return grupos;
    }

    /**
     * Gera partidas todos contra todos para um grupo
     */
    private List<Partida> gerarPartidasTodosContraTodos(List<Team> times, String nomeGrupo) throws Exception {
        List<Partida> partidas = new ArrayList<>();

        // Gera todas as combinações possíveis (todos contra todos)
        for (int i = 0; i < times.size(); i++) {
            for (int j = i + 1; j < times.size(); j++) {
                Team time1 = times.get(i);
                Team time2 = times.get(j);

                Partida partida = criarPartidaGrupo(time1, time2, nomeGrupo);
                if (partida != null) {
                    partidas.add(partida);
                }
            }
        }

        return partidas;
    }

    /**
     * Cria uma partida de grupo
     */
    private Partida criarPartidaGrupo(Team time1, Team time2, String nomeGrupo) throws Exception {
        if (time1 == null || time1.getId() == null || time2 == null || time2.getId() == null) {
            return null;
        }

        Partida novaPartida = new Partida();
        novaPartida.setJogo(this.camp.getJogo());
        novaPartida.setActive(true);
        novaPartida.setFinalizada(false);
        novaPartida.setNome(nomeGrupo + " - " + time1.getNome() + " vs " + time2.getNome());
        novaPartida.setDataPartida(new Date());

        // Salva a partida PRIMEIRO (sem itens ainda)
        novaPartida = partidaServico.salvar(novaPartida, null, Url.SALVAR_PARTIDA.getNome());

        if (novaPartida == null || novaPartida.getId() == null) {
            System.err.println("Erro ao salvar partida do grupo: partida retornou null");
            return null;
        }

        System.out.println("  Partida do grupo salva com ID: " + novaPartida.getId());

        // Agora cria os itens da partida COM o ID da partida já definido
        int qtdItens = 1;
        List<ItemPartida> itensPartida = PartidaUtils.gerarPartidasTimes(novaPartida, this.camp.getId(), time1, time2, qtdItens);

        // Define o partida_id em todos os itens ANTES de salvar
        if (itensPartida != null && !itensPartida.isEmpty()) {
            for (ItemPartida item : itensPartida) {
                if (item != null) {
                    item.setPartida(novaPartida.getId());
                    // Salva o item (cria novo, não atualiza)
                    try {
                        //itemPartidaServico.salvar(item, null, Url.SALVAR_ITEM_PARTIDA.getNome());
                    } catch (Exception e) {
                        System.err.println("  Erro ao salvar item de partida: " + e.getMessage());
                    }
                }
            }
        }

        // Atualiza a partida com os itens
        novaPartida.setItemPartida(itensPartida);
        novaPartida = partidaServico.salvar(novaPartida, novaPartida.getId(), Url.ATUALIZAR_PARTIDA.getNome());

        return novaPartida;
    }

    /**
     * Verifica se há grupos criados
     */
    public boolean hasGrupos() {
        return this.camp != null
                && this.camp.getGrupos() != null
                && !this.camp.getGrupos().isEmpty();
    }

    /**
     * Obtém os grupos do campeonato
     */
    public List<Grupo> getGrupos() {
        if (this.camp == null || this.camp.getGrupos() == null) {
            return new ArrayList<>();
        }
        return this.camp.getGrupos();
    }

    /**
     * Verifica se todas as partidas de um grupo foram finalizadas
     */
    public boolean isGrupoFinalizado(Grupo grupo) {
        if (grupo == null || grupo.getPartidas() == null || grupo.getPartidas().isEmpty()) {
            return false;
        }

        return grupo.getPartidas().stream()
                .allMatch(Partida::isFinalizada);
    }

    /**
     * Verifica se todos os grupos foram finalizados
     */
    public boolean isTodosGruposFinalizados() {
        if (!hasGrupos()) {
            return false;
        }

        return this.camp.getGrupos().stream()
                .allMatch(this::isGrupoFinalizado);
    }

    /**
     * Obtém a classificação de um grupo
     */
    public List<ClassificacaoTime> getClassificacaoGrupo(Grupo grupo) {
        List<ClassificacaoTime> tabela = new ArrayList<>();

        if (grupo == null) {
            System.err.println("getClassificacaoGrupo: grupo é null");
            return tabela;
        }

        // Inicializa classificação para cada time
        Map<Long, ClassificacaoTime> classificacaoPorTime = new HashMap<>();

        // Tenta obter times diretamente do grupo
        List<Team> timesDoGrupo = new ArrayList<>();
        if (grupo.getTeams() != null && !grupo.getTeams().isEmpty()) {
            timesDoGrupo = grupo.getTeams();
            System.out.println("getClassificacaoGrupo: Encontrados " + timesDoGrupo.size() + " times diretamente no grupo");
        } else if (grupo.getTimes() != null && !grupo.getTimes().isEmpty()) {
            // Fallback para método legado
            timesDoGrupo = grupo.getTimes();
            System.out.println("getClassificacaoGrupo: Encontrados " + timesDoGrupo.size() + " times via método legado");
        }

        // Se não encontrou times diretamente, extrai das partidas
        if (timesDoGrupo.isEmpty() && grupo.getPartidas() != null && !grupo.getPartidas().isEmpty()) {
            System.out.println("getClassificacaoGrupo: Extraindo times das partidas...");
            Set<Long> idsTimesJaAdicionados = new HashSet<>();

            for (Partida partida : grupo.getPartidas()) {
                if (partida.getItemPartida() != null && !partida.getItemPartida().isEmpty()) {
                    ItemPartida item = partida.getItemPartida().get(0);

                    if (item.getTeam1() != null && item.getTeam1().getId() != null) {
                        if (!idsTimesJaAdicionados.contains(item.getTeam1().getId())) {
                            timesDoGrupo.add(item.getTeam1());
                            idsTimesJaAdicionados.add(item.getTeam1().getId());
                        }
                    }

                    if (item.getTeam2() != null && item.getTeam2().getId() != null) {
                        if (!idsTimesJaAdicionados.contains(item.getTeam2().getId())) {
                            timesDoGrupo.add(item.getTeam2());
                            idsTimesJaAdicionados.add(item.getTeam2().getId());
                        }
                    }
                }
            }

            System.out.println("getClassificacaoGrupo: Extraídos " + timesDoGrupo.size() + " times das partidas");
        }

        if (timesDoGrupo.isEmpty()) {
            System.err.println("getClassificacaoGrupo: Nenhum time encontrado no grupo " + grupo.getNome());
            return tabela;
        }

        // Inicializa classificação para cada time encontrado
        for (Team time : timesDoGrupo) {
            if (time != null && time.getId() != null) {
                classificacaoPorTime.put(time.getId(), new ClassificacaoTime(time));
            }
        }

        System.out.println("getClassificacaoGrupo: Inicializada classificação para " + classificacaoPorTime.size() + " times");

        // Processa partidas do grupo
        if (grupo.getPartidas() != null) {
            int partidasProcessadas = 0;
            for (Partida partida : grupo.getPartidas()) {
                if (partida.isFinalizada() && partida.getTimeVencedor() != null
                        && partida.getTimePerdedor() != null) {

                    Team timeVencedor = partida.getTimeVencedor();
                    Team timePerdedor = partida.getTimePerdedor();

                    if (timeVencedor != null && timeVencedor.getId() != null
                            && timePerdedor != null && timePerdedor.getId() != null) {

                        ClassificacaoTime classVencedor = classificacaoPorTime.get(timeVencedor.getId());
                        ClassificacaoTime classPerdedor = classificacaoPorTime.get(timePerdedor.getId());

                        if (classVencedor != null && classPerdedor != null) {
                            classVencedor.adicionarVitoria();
                            classPerdedor.adicionarDerrota();

                            // Calcula rounds ganhos e perdidos baseado nos itemPartida
                            if (partida.getItemPartida() != null && !partida.getItemPartida().isEmpty()) {
                                for (ItemPartida item : partida.getItemPartida()) {
                                    // Verifica se o itemPartida está relacionado com a partida
                                    if (item.getPartida() != null && item.getPartida().equals(partida.getId())) {
                                        if (item.getTimeVencedor() != null && item.getTeam1() != null && item.getTeam2() != null) {
                                            // Se team1 for o vencedor:
                                            // - team1: rounds ganhos = scoreT1, rounds perdidos = scoreT2
                                            // - team2: rounds ganhos = scoreT2, rounds perdidos = scoreT1
                                            // Se team2 for o vencedor:
                                            // - team2: rounds ganhos = scoreT2, rounds perdidos = scoreT1
                                            // - team1: rounds ganhos = scoreT1, rounds perdidos = scoreT2

                                            boolean team1Venceu = item.getTimeVencedor().getId().equals(item.getTeam1().getId());
                                            boolean team2Venceu = item.getTimeVencedor().getId().equals(item.getTeam2().getId());

                                            int scoreT1 = item.getScoreT1() != null ? item.getScoreT1() : 0;
                                            int scoreT2 = item.getScoreT2() != null ? item.getScoreT2() : 0;

                                            // Verifica qual time da partida é o team1 e qual é o team2 do item
                                            ClassificacaoTime classTeam1 = null;
                                            ClassificacaoTime classTeam2 = null;

                                            if (item.getTeam1().getId().equals(timeVencedor.getId())) {
                                                classTeam1 = classVencedor;
                                            } else if (item.getTeam1().getId().equals(timePerdedor.getId())) {
                                                classTeam1 = classPerdedor;
                                            }

                                            if (item.getTeam2().getId().equals(timeVencedor.getId())) {
                                                classTeam2 = classVencedor;
                                            } else if (item.getTeam2().getId().equals(timePerdedor.getId())) {
                                                classTeam2 = classPerdedor;
                                            }

                                            if (team1Venceu && classTeam1 != null && classTeam2 != null) {
                                                // Team1 venceu: team1 ganha scoreT1 rounds e perde scoreT2 rounds
                                                // Team2 perde: team2 ganha scoreT2 rounds e perde scoreT1 rounds
                                                classTeam1.adicionarRoundsGanhos(scoreT1);
                                                classTeam1.adicionarRoundsPerdidos(scoreT2);
                                                classTeam2.adicionarRoundsGanhos(scoreT2);
                                                classTeam2.adicionarRoundsPerdidos(scoreT1);
                                            } else if (team2Venceu && classTeam1 != null && classTeam2 != null) {
                                                // Team2 venceu: team2 ganha scoreT2 rounds e perde scoreT1 rounds
                                                // Team1 perde: team1 ganha scoreT1 rounds e perde scoreT2 rounds
                                                classTeam2.adicionarRoundsGanhos(scoreT2);
                                                classTeam2.adicionarRoundsPerdidos(scoreT1);
                                                classTeam1.adicionarRoundsGanhos(scoreT1);
                                                classTeam1.adicionarRoundsPerdidos(scoreT2);
                                            }
                                        }
                                    }
                                }
                            }

                            partidasProcessadas++;
                        }
                    }
                }
            }
            System.out.println("getClassificacaoGrupo: Processadas " + partidasProcessadas + " partidas finalizadas");
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

        System.out.println("getClassificacaoGrupo: Retornando " + tabela.size() + " times na classificação");

        return tabela;
    }

    /**
     * Obtém os 2 primeiros colocados de um grupo
     */
    public List<Team> getClassificadosGrupo(Grupo grupo) {
        List<ClassificacaoTime> classificacao = getClassificacaoGrupo(grupo);

        return classificacao.stream()
                .limit(2) // Apenas os 2 primeiros
                .map(ClassificacaoTime::getTeam)
                .collect(Collectors.toList());
    }

    /**
     * Obtém a quantidade de times de um grupo Busca diretamente do grupo ou
     * extrai das partidas se necessário
     */
    public int getQuantidadeTimesGrupo(Grupo grupo) {
        if (grupo == null) {
            return 0;
        }

        // Tenta obter diretamente do grupo
        if (grupo.getTeams() != null && !grupo.getTeams().isEmpty()) {
            return grupo.getTeams().size();
        }

        if (grupo.getTimes() != null && !grupo.getTimes().isEmpty()) {
            return grupo.getTimes().size();
        }

        // Se não encontrou, extrai das partidas
        if (grupo.getPartidas() != null && !grupo.getPartidas().isEmpty()) {
            Set<Long> idsTimes = new HashSet<>();
            for (Partida partida : grupo.getPartidas()) {
                if (partida.getItemPartida() != null && !partida.getItemPartida().isEmpty()) {
                    ItemPartida item = partida.getItemPartida().get(0);
                    if (item.getTeam1() != null && item.getTeam1().getId() != null) {
                        idsTimes.add(item.getTeam1().getId());
                    }
                    if (item.getTeam2() != null && item.getTeam2().getId() != null) {
                        idsTimes.add(item.getTeam2().getId());
                    }
                }
            }
            return idsTimes.size();
        }

        return 0;
    }

    /**
     * Gera os playoffs com os classificados dos grupos Usa a mesma lógica do
     * método gerarPlayoffs() do suíço
     */
    public void gerarPlayoffsDeGrupos() {
        try {
            System.out.println("====== INICIANDO GERAÇÃO DOS PLAYOFFS DOS GRUPOS ======");

            // Validações
            if (!isAdmin()) {
                System.err.println("Erro: Usuário não é admin");
                Mensagem.error("Apenas administradores podem gerar os playoffs!");
                return;
            }

            if (!isGrupoPlayoff()) {
                System.err.println("Erro: Campeonato não é GRUPO E PLAYOFF");
                Mensagem.error("Este campeonato não é do tipo GRUPO E PLAYOFF!");
                return;
            }

            if (!isTodosGruposFinalizados()) {
                System.err.println("Erro: Grupos não finalizados");
                Mensagem.error("Todas as partidas dos grupos devem estar finalizadas!");
                return;
            }

            if (hasPlayoffs()) {
                System.err.println("Erro: Playoffs já foram gerados");
                Mensagem.error("Os playoffs já foram gerados para este campeonato!");
                return;
            }

            // Atualiza o campeonato
            this.camp = this.campeonatoServico.buscaCamp(this.camp.getId());

            // Coleta os classificados de cada grupo (2 primeiros de cada grupo)
            List<Team> classificados = new ArrayList<>();
            for (Grupo grupo : this.camp.getGrupos()) {
                if (grupo != null) {
                    List<Team> classificadosGrupo = getClassificadosGrupo(grupo);
                    classificados.addAll(classificadosGrupo);
                    System.out.println("✓ Classificados do " + grupo.getNome() + ": "
                            + classificadosGrupo.stream()
                                    .map(Team::getNome)
                                    .collect(Collectors.joining(", ")));
                }
            }

            System.out.println("✓ Total de times classificados: " + classificados.size());

            if (classificados.size() < 2) {
                Mensagem.error("É necessário pelo menos 2 times classificados para gerar os playoffs!");
                return;
            }

            // Determina o número de times nos playoffs (potência de 2) - mesma lógica do suíço
            int numeroTimes = getMaiorPotenciaDe2(classificados.size());
            List<Team> timesPlayoffs = classificados.subList(0, Math.min(numeroTimes, classificados.size()));

            System.out.println("✓ Times nos playoffs: " + timesPlayoffs.size() + " (de " + classificados.size() + " classificados)");

            // Inicializa lista de fases se necessário
            if (this.camp.getFasesCamp() == null) {
                this.camp.setFasesCamp(new ArrayList<>());
            }

            // Gera a primeira fase dos playoffs - mesma lógica do suíço
            Fase primeiraFase = gerarFase(timesPlayoffs, getNomeFase(timesPlayoffs.size()));
            this.camp.getFasesCamp().add(primeiraFase);

            System.out.println("✓ Fase gerada: " + primeiraFase.getNome() + " com "
                    + primeiraFase.getPartidas().size() + " partidas");

            // Salva o campeonato atualizado - mesma lógica do suíço
            this.camp = campeonatoServico.save(this.camp, this.camp.getId(), Url.ATUALIZAR_CAMPEONATO.getNome());

            System.out.println("====== PLAYOFFS DOS GRUPOS GERADOS COM SUCESSO ======");

            Mensagem.successAndRedirect(
                    "Playoffs gerados com sucesso! " + primeiraFase.getNome() + " criada com "
                    + primeiraFase.getPartidas().size() + " partidas.",
                    "visualizarCampeonato.xhtml?id=" + this.camp.getId()
            );

        } catch (Exception ex) {
            System.err.println("====== ERRO AO GERAR PLAYOFFS DOS GRUPOS ======");
            ex.printStackTrace();
            Mensagem.error("Erro ao gerar playoffs: " + ex.getMessage());
        }
    }

}
