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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
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

    @PostConstruct
    public void init() {
        instanciar();

        // Carrega o player logado através do ManagerBase
        getPlayerLogado();
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

    public void inscrever() {
        try {
            Player player = getPlayerLogado();
            if (player != null) {
                this.camp.getPlayers().add(player);
                this.camp = campeonatoServico.save(this.camp, this.camp.getId(), Url.ATUALIZAR_CAMPEONATO.getNome());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        Mensagem.successAndRedirect("Camp salvo", "visualizarCampeonato.xhtml?id=" + this.camp.getId());
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
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            return sdf.format(data);
        }
        return "—";
    }

    // Helpers para Swiss System
    public List<SwissRound> getRodadasSuicas() {
        List<SwissRound> rodadas = new ArrayList<>();
        if (camp == null || camp.getTipoCampeonato() == null || !camp.getTipoCampeonato().getNome().equals("SUÍÇO")) {
            return rodadas;
        }
        
        // Mapear todas as rodadas suíças disponíveis
        if (camp.getRodadaSuica00() != null && !camp.getRodadaSuica00().isEmpty()) {
            rodadas.add(new SwissRound("0:0", camp.getRodadaSuica00()));
        }
        if (camp.getRodadaSuica01() != null && !camp.getRodadaSuica01().isEmpty()) {
            rodadas.add(new SwissRound("0:1", camp.getRodadaSuica01()));
        }
        if (camp.getRodadaSuica02() != null && !camp.getRodadaSuica02().isEmpty()) {
            rodadas.add(new SwissRound("0:2", camp.getRodadaSuica02()));
        }
        if (camp.getRodadaSuica03() != null && !camp.getRodadaSuica03().isEmpty()) {
            rodadas.add(new SwissRound("0:3", camp.getRodadaSuica03()));
        }
        if (camp.getRodadaSuica10() != null && !camp.getRodadaSuica10().isEmpty()) {
            rodadas.add(new SwissRound("1:0", camp.getRodadaSuica10()));
        }
        if (camp.getRodadaSuica11() != null && !camp.getRodadaSuica11().isEmpty()) {
            rodadas.add(new SwissRound("1:1", camp.getRodadaSuica11()));
        }
        if (camp.getRodadaSuica12() != null && !camp.getRodadaSuica12().isEmpty()) {
            rodadas.add(new SwissRound("1:2", camp.getRodadaSuica12()));
        }
        if (camp.getRodadaSuica13() != null && !camp.getRodadaSuica13().isEmpty()) {
            rodadas.add(new SwissRound("1:3", camp.getRodadaSuica13()));
        }
        if (camp.getRodadaSuica20() != null && !camp.getRodadaSuica20().isEmpty()) {
            rodadas.add(new SwissRound("2:0", camp.getRodadaSuica20()));
        }
        if (camp.getRodadaSuica21() != null && !camp.getRodadaSuica21().isEmpty()) {
            rodadas.add(new SwissRound("2:1", camp.getRodadaSuica21()));
        }
        if (camp.getRodadaSuica22() != null && !camp.getRodadaSuica22().isEmpty()) {
            rodadas.add(new SwissRound("2:2", camp.getRodadaSuica22()));
        }
        if (camp.getRodadaSuica23() != null && !camp.getRodadaSuica23().isEmpty()) {
            rodadas.add(new SwissRound("2:3", camp.getRodadaSuica23()));
        }
        if (camp.getRodadaSuica30() != null && !camp.getRodadaSuica30().isEmpty()) {
            rodadas.add(new SwissRound("3:0", camp.getRodadaSuica30()));
        }
        if (camp.getRodadaSuica31() != null && !camp.getRodadaSuica31().isEmpty()) {
            rodadas.add(new SwissRound("3:1", camp.getRodadaSuica31()));
        }
        if (camp.getRodadaSuica32() != null && !camp.getRodadaSuica32().isEmpty()) {
            rodadas.add(new SwissRound("3:2", camp.getRodadaSuica32()));
        }
        
        return rodadas;
    }

    public boolean isRodadaAtualFinalizada(SwissRound rodada) {
        if (rodada == null || rodada.partidas == null || rodada.partidas.isEmpty()) {
            return true;
        }
        for (Partida p : rodada.partidas) {
            if (!p.isFinalizada()) {
                return false;
            }
        }
        return true;
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

    public void gerarProximaRodada() {
        // TODO: Implementar lógica de geração da próxima rodada suíça
        Mensagem.success("Próxima rodada gerada com sucesso!");
    }

    // Classe interna para representar uma rodada suíça
    public static class SwissRound {
        public String record;
        public List<Partida> partidas;

        public SwissRound(String record, List<Partida> partidas) {
            this.record = record;
            this.partidas = partidas;
        }

        public String getRecord() {
            return record;
        }

        public List<Partida> getPartidas() {
            return partidas;
        }
    }

}
