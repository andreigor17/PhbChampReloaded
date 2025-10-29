package br.com.champ.Manager;

import br.com.champ.Enums.Url;
import br.com.champ.Modelo.Campeonato;
import br.com.champ.Modelo.Estatisticas;
import br.com.champ.Modelo.Partida;
import br.com.champ.Modelo.Player;
import br.com.champ.Modelo.Team;
import br.com.champ.Servico.CampeonatoServico;
import br.com.champ.Servico.EstatisticaServico;
import br.com.champ.Servico.LoginServico;
import br.com.champ.Servico.PartidaServico;
import br.com.champ.Servico.PlayerServico;
import br.com.champ.Servico.TeamServico;
import br.com.champ.Utilitario.FacesUtil;
import br.com.champ.Utilitario.Mensagem;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpServletRequest;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 *
 * @author andre
 */
@ViewScoped
@Named
public class ManagerCamp implements Serializable {

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
    LoginServico loginServico;

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
    private Player playerLogado;
    private List<Partida> oitavas;
    private List<Partida> quartas;
    private List<Partida> semis;
    private List<Partida> finais;
    private List<Partida> terceiroLugar;

    @PostConstruct
    public void init() {
        instanciar();

        this.playerLogado = loginServico.obterPlayerId();
        String visualizarCampId = FacesUtil
                .getRequestParameter("id");

        if (visualizarCampId != null && !visualizarCampId.isEmpty()) {
            this.camp = this.campeonatoServico.buscaCamp(Long.parseLong(visualizarCampId));
            this.partidas = partidaServico.partidaPorCamp(this.camp.getId());
        }

        if (this.camp.getId() != null) {
            for (Team timeCamp : this.camp.getTeams()) {
                this.estatisticasTime = estatisticaServico.estatisticaPorTime(timeCamp.getId(), this.camp.getId());
                for (Estatisticas estats : this.estatisticasTime) {
                    this.ests.add(estats);
                }
            }
        }

        if (this.camp.getTeams() != null && !this.camp.getTeams().isEmpty()) {
            this.mvp = somaEstsPlayersTop().get(0);
        }

        HttpServletRequest uri = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();

        if (uri.getRequestURI().contains("indexCampeonato.xhtml")) {
            try {
                this.camps = campeonatoServico.pesquisar();
            } catch (Exception ex) {
                Logger.getLogger(ManagerCamp.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

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
        this.playerLogado = null;
        this.oitavas = new ArrayList<>();
        this.quartas = new ArrayList<>();
        this.semis = new ArrayList<>();
        this.finais = new ArrayList<>();
        this.terceiroLugar = new ArrayList<>();

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
        instanciar();
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

    public Player getPlayerLogado() {
        return playerLogado;
    }

    public void setPlayerLogado(Player playerLogado) {
        this.playerLogado = playerLogado;
    }

    public void inscrever() {
        try {
            this.camp.getPlayers().add(this.playerLogado);
            this.camp = campeonatoServico.save(this.camp, this.camp.getId(), Url.ATUALIZAR_CAMPEONATO.getNome());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        Mensagem.successAndRedirect("Camp salvo", "visualizarCampeonato.xhtml?id=" + this.camp.getId());
    }

    public static void gerarRodada(Campeonato camp, int rodada) {
        List<Estatisticas> estatisticas = camp.getEstatisticas();
        List<Team> times = camp.getTeams();

        // Mapa de vitórias por time
        Map<Long, Integer> vitPorTime = estatisticas.stream()
                .collect(Collectors.toMap(e -> e.getTeam().getId(), Estatisticas::getPartidasGanhas));

        // Mapa de derrotas por time
        Map<Long, Integer> derPorTime = estatisticas.stream()
                .collect(Collectors.toMap(e -> e.getTeam().getId(), Estatisticas::getPartidasPerdidas));

        // Agrupa por vitórias
        Map<Integer, List<Team>> grupos = times.stream()
                .collect(Collectors.groupingBy(t -> vitPorTime.getOrDefault(t.getId(), 0)));

        for (Map.Entry<Integer, List<Team>> entry : grupos.entrySet()) {
            int vitorias = entry.getKey();
            List<Team> grupo = new ArrayList<>(entry.getValue());
            Collections.shuffle(grupo); // sorteia

            while (grupo.size() >= 2) {
                Team t1 = grupo.remove(0);
                Team t2 = grupo.remove(0);

                Partida partida = new Partida();
                partida.getTeams().add(t1);
                partida.getTeams().add(t2);

                int v1 = vitPorTime.getOrDefault(t1.getId(), 0);
                int d1 = derPorTime.getOrDefault(t1.getId(), 0);
                int v2 = vitPorTime.getOrDefault(t2.getId(), 0);
                int d2 = derPorTime.getOrDefault(t2.getId(), 0);

                // Escolhe a lista de acordo com o estado (ex: 1–0, 1–1 etc.)
                adicionarNaListaCorreta(camp, partida, v1, d1, v2, d2);
            }

            // Se sobrou um time (ímpar), você pode dar um "bye" (opcional)
            if (!grupo.isEmpty()) {
                Team sobrando = grupo.get(0);
                System.out.println("Time sobrando nesta rodada (" + vitorias + " vitórias): " + sobrando.getNome());
            }
        }
    }

    private static void adicionarNaListaCorreta(Campeonato camp, Partida partida,
            int v1, int d1, int v2, int d2) {
        // Mesma pontuação entre os dois (ex: 1–0 vs 1–0)
        String chave = v1 + "" + d1;
        switch (chave) {
            case "00" ->
                camp.getRodadaSuica00().add(partida);
            case "10" ->
                camp.getRodadaSuica10().add(partida);
            case "01" ->
                camp.getRodadaSuica01().add(partida);
            case "20" ->
                camp.getRodadaSuica20().add(partida);
            case "11" ->
                camp.getRodadaSuica11().add(partida);
            case "02" ->
                camp.getRodadaSuica02().add(partida);
            case "30" ->
                camp.getRodadaSuica30().add(partida);
            case "21" ->
                camp.getRodadaSuica21().add(partida);
            case "12" ->
                camp.getRodadaSuica12().add(partida);
            case "31" ->
                camp.getRodadaSuica31().add(partida);
            case "22" ->
                camp.getRodadaSuica22().add(partida);
            case "13" ->
                camp.getRodadaSuica13().add(partida);
            case "32" ->
                camp.getRodadaSuica32().add(partida);
            case "23" ->
                camp.getRodadaSuica23().add(partida);
            case "03" ->
                camp.getRodadaSuica03().add(partida);
            default ->
                System.out.println("Rodada desconhecida: " + chave);
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

}
