package br.com.champ.Manager;

import br.com.champ.Enums.Url;
import br.com.champ.Modelo.Campeonato;
import br.com.champ.Modelo.Estatisticas;
import br.com.champ.Modelo.Partida;
import br.com.champ.Modelo.Player;
import br.com.champ.Modelo.Team;
import br.com.champ.Servico.CampeonatoServico;
import br.com.champ.Servico.EstatisticaServico;
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
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


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

    @PostConstruct
    public void init() {
        instanciar();

        String visualizarCampId = FacesUtil
                .getRequestParameter("id");

        String visualizarPreCampId = FacesUtil
                .getRequestParameter("preCampId");

        if (visualizarCampId != null && !visualizarCampId.isEmpty()) {
            this.camp = this.campeonatoServico.buscaCamp(Long.parseLong(visualizarCampId));
            this.partidas = partidaServico.partidaPorCamp(this.camp.getId());
        }

        if (visualizarPreCampId != null && !visualizarPreCampId.isEmpty()) {
            this.preCamp = this.campeonatoServico.buscaCamp(Long.parseLong(visualizarPreCampId));
        }

        if (this.camp.getId() != null) {
            for (Team timeCamp : this.camp.getTeams()) {
                this.estatisticasTime = estatisticaServico.estatisticaPorTime(timeCamp.getId(), this.camp.getId());
                for (Estatisticas estats : this.estatisticasTime) {
                    this.ests.add(estats);
                }
            }
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

    public List<Estatisticas> somaEstsTime() {
        List<Estatisticas> soma = new ArrayList<>();
        Estatisticas est = new Estatisticas();
        Integer kills = 0;
        Integer deaths = 0;
        Integer assists = 0;
        Integer pontos = 0;

        for (Team timeCamp : this.camp.getTeams()) {
            this.estatisticasTime = estatisticaServico.estatisticaPorTime(timeCamp.getId(), this.camp.getId());
            for (Estatisticas estats : this.estatisticasTime) {
                if (estats.getTeam().getId().equals(timeCamp.getId())) {
                    kills += estats.getKills();
                    deaths += estats.getDeaths();
                    assists += estats.getAssists();
                }

            }

            est.setKills(kills);
            est.setAssists(assists);
            est.setDeaths(deaths);
            est.setTeam(timeCamp);
            soma.add(est);
            kills = 0;
            deaths = 0;
            assists = 0;
            est = new Estatisticas();
        }
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

}
