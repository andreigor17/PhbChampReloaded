/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.champ.Manager;

import br.com.champ.Enums.Url;
import br.com.champ.Modelo.Estatisticas;
import br.com.champ.Modelo.ItemPartida;
import br.com.champ.Modelo.Mapas;
import br.com.champ.Modelo.Partida;
import br.com.champ.Modelo.Player;
import br.com.champ.Modelo.Team;
import br.com.champ.Servico.EstatisticaServico;
import br.com.champ.Servico.ItemPartidaServico;
import br.com.champ.Servico.LoginServico;
import br.com.champ.Servico.MapaServico;
import br.com.champ.Servico.PartidaServico;
import br.com.champ.Servico.PlayerServico;
import br.com.champ.Servico.TeamServico;
import br.com.champ.Utilitario.FacesUtil;
import br.com.champ.Utilitario.Mensagem;
import br.com.champ.Utilitario.PartidaUtils;
import br.com.champ.Utilitario.PickBanUtils;
import br.com.champ.Utilitario.Utils;
import br.com.champ.vo.PickBanVo;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpServletRequest;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.primefaces.PrimeFaces;
import org.primefaces.event.DragDropEvent;
import org.primefaces.model.DualListModel;
import org.primefaces.model.charts.ChartData;
import org.primefaces.model.charts.optionconfig.elements.Elements;
import org.primefaces.model.charts.optionconfig.elements.ElementsLine;
import org.primefaces.model.charts.radar.RadarChartDataSet;
import org.primefaces.model.charts.radar.RadarChartModel;
import org.primefaces.model.charts.radar.RadarChartOptions;

/**
 *
 * @author andre
 */
@ViewScoped
@Named
public class ManagerPartida implements Serializable {

    @EJB
    private PartidaServico partidaServico;
    @EJB
    ItemPartidaServico itemPartidaServico;
    @EJB
    PlayerServico playerServico;
    @EJB
    TeamServico teamServico;
    @EJB
    EstatisticaServico estatisticasServico;
    @EJB
    MapaServico mapaServico;
    @EJB
    LoginServico loginServico;
    private Partida partida;
    private Partida partidaPesquisar;
    private List<Player> playersTime1;
    private List<Player> playersTime2;
    private List<Partida> partidas;
    private List<ItemPartida> itensPartidas;
    private ItemPartida itemPartida;
    private int qtdItensPartidas;
    private String capitaoTime1;
    private String capitaoTime2;
    private String nomeTime1;
    private String nomeTime2;
    private List<ItemPartida> itemPartidas;
    private List<Player> allPlayers;
    private List<Player> selectedPlayers;
    private DualListModel<Player> playerGroupList;
    private Player player;
    private List<Player> players;
    private List<Estatisticas> estsGerais;
    private boolean skip;
    private List<Mapas> mapas;
    private List<Mapas> pickedMaps;
    List<String> picksbans;
    private List<PickBanVo> pickBanVo;
    private PickBanVo pbItem;
    private boolean classica = false;
    private int tipoEscolhaCapitaes;
    private List<Player> droppedPlayers1;
    private List<Player> droppedPlayers2;
    private Player selectedPlayer;
    private List<Player> pickedPlayers;
    private int scoreT1;
    private int scoreT2;
    private Team time1;
    private Team time2;
    private List<Team> timesPartida;
    private Team teamVencedor;
    private RadarChartModel radarModel;
    private boolean edicao;
    private String horarioPartida;
    private int totalCheckins;
    private boolean jaFezCheckin;
    private boolean podeParticipar;
    private String horaFormatadaPartida;
    private String dataFormatadaPartida;
    private int percentualParticipantes;
    private String tempoRestante;
    private BigDecimal valorFormatado;
    private Player playerLogado;
    private Team timeIniciante;

    @PostConstruct
    public void init() {
        try {
            instanciar();
            String visualizarPartidaId = FacesUtil
                    .getRequestParameter("id");
            String gerarMapasId = FacesUtil
                    .getRequestParameter("partidaId");

            this.playerLogado = loginServico.obterPlayerId();
            //System.err.println("Tem player logado... " + this.playerLogado.getId());

            Map<String, String> parametros = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
            HttpServletRequest uri = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
            this.classica = parametros.get("classica") != null;

            if (uri.getRequestURI().contains("partidaClassica.xhtml")) {
                System.err.println("classica");
                PrimeFaces.current().executeScript("PF('selecaoDeCapitaesDialog').show();");
            }

            if (visualizarPartidaId != null && !visualizarPartidaId.isEmpty()) {
                this.partida = this.partidaServico.pesquisar(Long.parseLong(visualizarPartidaId));
            } else if (gerarMapasId != null && !gerarMapasId.isEmpty()) {
                this.partida = this.partidaServico.pesquisar(Long.parseLong(gerarMapasId));
            } else {
                try {
                    instanciar();
                } catch (Exception ex) {
                    System.err.println(ex);
                }
            }

            if (this.partida.getId() != null && !uri.getRequestURI().contains("partida-futebol-view.xhtml")) {
                try {
                    this.itensPartidas = this.partida.getItemPartida();
                    this.mapas = mapaServico.pesquisar();                    
                    
                    gerarScore();
                    createRadarModel();
                } catch (Exception ex) {
                    System.err.println(ex);
                }
            } else if (this.partida.getId() != null && uri.getRequestURI().contains("partida-futebol-view.xhtml")) {
                OffsetDateTime odt = OffsetDateTime.parse(this.partida.getDataCriacao());

                dataFormatadaPartida = odt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                horaFormatadaPartida = odt.format(DateTimeFormatter.ofPattern("HH:mm"));
            }
        } catch (Exception ex) {
            System.err.println(ex);
        }

    }

    public void instanciar() throws Exception {
        this.partida = new Partida();
        this.partidas = new ArrayList<>();
        this.partidaPesquisar = new Partida();
        this.itemPartida = new ItemPartida();
        this.itensPartidas = new ArrayList<>();
        this.selectedPlayers = new ArrayList<Player>();
        this.allPlayers = playerServico.buscaPlayers();
        this.playerGroupList = new DualListModel<>(this.allPlayers, this.selectedPlayers);
        this.itemPartidas = new ArrayList<>();
        this.itemPartida = new ItemPartida();
        this.estsGerais = new ArrayList<Estatisticas>();
        this.mapas = new ArrayList<>();
        this.pickedMaps = new ArrayList<>();
        this.droppedPlayers1 = new ArrayList<>();
        this.droppedPlayers2 = new ArrayList<>();
        this.selectedPlayer = new Player();
        this.selectedPlayers = new ArrayList<>();
        this.time1 = new Team();
        this.time2 = new Team();
        this.timesPartida = new ArrayList<>();
        this.teamVencedor = new Team();
        this.radarModel = new RadarChartModel();
        this.playerLogado = new Player();
        this.nomeTime1 = null;
        this.nomeTime2 = null;
        this.timeIniciante = null;
    }

    public void createRadarModel() throws Exception {
        radarModel = new RadarChartModel();
        ChartData data = new ChartData();
        List<String> labels = new ArrayList<>();

        RadarChartDataSet radarDataSet = new RadarChartDataSet();
        radarDataSet.setLabel(this.partida.getItemPartida().get(0).getTeam1().getNome());
        radarDataSet.setFill(true);
        radarDataSet.setBackgroundColor("rgba(255, 99, 132, 0.2)");
        radarDataSet.setBorderColor("rgb(255, 99, 132)");
        radarDataSet.setPointBackgroundColor("rgb(255, 99, 132)");
        radarDataSet.setPointBorderColor("#fff");
        radarDataSet.setPointHoverBackgroundColor("#fff");
        radarDataSet.setPointHoverBorderColor("rgb(255, 99, 132)");

        List<Mapas> mapas = mapaServico.pesquisarOrdenado();

        for (Mapas m : mapas) {
            labels.add(m.getNome());
        }

        Map<Mapas, Integer> mapaCount = new HashMap<>();
        Map<Mapas, Integer> mapaCount2 = new HashMap<>();

        List<ItemPartida> partidas = itemPartidaServico.pesquisarItensTimeVencedor(this.partida.getItemPartida().get(0).getTeam1().getId());
        if (Utils.isNotEmpty(partidas)) {
            for (ItemPartida item : partidas) {
                mapaCount.put(item.getMapas(), mapaCount.getOrDefault(item.getMapas(), 0) + 1);

            }
        }

        List<Number> values = new ArrayList<>();

        System.err.println("TIME 1");
        for (Map.Entry<Mapas, Integer> entry : mapaCount.entrySet()) {
            Integer valor = entry.getValue();
            for (String s : labels) {
                if (entry.getKey().getNome().equalsIgnoreCase(s)) {
                    values.add(valor);
                } else {
                    values.add(0);
                }
            }
        }
        radarDataSet.setData(values);

        RadarChartDataSet radarDataSet2 = new RadarChartDataSet();
        radarDataSet2.setLabel(this.partida.getItemPartida().get(0).getTeam2().getNome());
        radarDataSet2.setFill(true);
        radarDataSet2.setBackgroundColor("rgba(54, 162, 235, 0.2)");
        radarDataSet2.setBorderColor("rgb(54, 162, 235)");
        radarDataSet2.setPointBackgroundColor("rgb(54, 162, 235)");
        radarDataSet2.setPointBorderColor("#fff");
        radarDataSet2.setPointHoverBackgroundColor("#fff");
        radarDataSet2.setPointHoverBorderColor("rgb(54, 162, 235)");
        List<ItemPartida> partidas2 = itemPartidaServico.pesquisarItensTimeVencedor(this.partida.getItemPartida().get(0).getTeam2().getId());
        if (Utils.isNotEmpty(partidas2)) {
            for (ItemPartida item : partidas2) {
                mapaCount2.put(item.getMapas(), mapaCount2.getOrDefault(item.getMapas(), 0) + 1);

            }
        }

        System.err.println("TIME 2");
        List<Number> values2 = new ArrayList<>();
        for (Map.Entry<Mapas, Integer> entry : mapaCount2.entrySet()) {
            Integer valor = entry.getValue();
            for (String s : labels) {
                if (entry.getKey().getNome().equalsIgnoreCase(s)) {
                    values2.add(valor);
                } else {
                    values2.add(0);
                }
            }
        }

        data.setLabels(labels);
        radarDataSet2.setData(values2);

        data.addChartDataSet(radarDataSet);
        data.addChartDataSet(radarDataSet2);

        /* Options */
        RadarChartOptions options = new RadarChartOptions();
        Elements elements = new Elements();
        ElementsLine elementsLine = new ElementsLine();
        elementsLine.setTension(0);
        elementsLine.setBorderWidth(3);
        elements.setLine(elementsLine);
        options.setElements(elements);

        radarModel.setOptions(options);
        radarModel.setData(data);
    }

    public void gerarScore() {
        for (ItemPartida ip : this.partida.getItemPartida()) {
            if (ip.getTimeVencedor() != null) {
                if (ip.getTimeVencedor().getId().equals(ip.getTeam1().getId())) {
                    scoreT1 = scoreT1 + 1;
                } else {
                    scoreT2 = scoreT2 + 1;
                }
            }
        }
    }

    public List<Estatisticas> estsGeraisTeam(Team team, ItemPartida item) {
        return estatisticasServico.estatisticaPorItemPartidaTeam(team.getId(), item.getId());
    }

    public List<Estatisticas> estsGeraisPlayer(Player player, ItemPartida item) {
        return estatisticasServico.estatisticaPorItemPartidaTeam(player.getId(), item.getId());
    }

    public Team getTime1() {
        return time1;
    }

    public void setTime1(Team time1) {
        this.time1 = time1;
    }

    public Team getTime2() {
        return time2;
    }

    public void setTime2(Team time2) {
        this.time2 = time2;
    }

    public List<Team> getTimesPartida() {
        return timesPartida;
    }

    public void setTimesPartida(List<Team> timesPartida) {
        this.timesPartida = timesPartida;
    }

    public int getScoreT1() {
        return scoreT1;
    }

    public Team getTeamVencedor() {
        return teamVencedor;
    }

    public void setTeamVencedor(Team teamVencedor) {
        this.teamVencedor = teamVencedor;
    }

    public void setScoreT1(int scoreT1) {
        this.scoreT1 = scoreT1;
    }

    public int getScoreT2() {
        return scoreT2;
    }

    public void setScoreT2(int scoreT2) {
        this.scoreT2 = scoreT2;
    }

    public RadarChartModel getRadarModel() {
        return radarModel;
    }

    public void setRadarModel(RadarChartModel radarModel) {
        this.radarModel = radarModel;
    }

    public List<Estatisticas> somaEstsTeam(Team team) {
        List<Estatisticas> soma = new ArrayList<>();
        Estatisticas est = new Estatisticas();
        Integer kills = 0;
        Integer deaths = 0;
        Integer assists = 0;
        List<Estatisticas> ests = estatisticasServico.estatisticaPorPartidaTeam(team.getId(), this.partida.getId());
        //System.err.println("size " + ests.size());
        for (Player p : team.getPlayers()) {
            for (Estatisticas e : ests) {
                if (e.getPlayer().getId().equals(p.getId())) {
                    kills += e.getKills();
                    deaths += e.getDeaths();
                    assists += e.getAssists();

                }

            }
            est.setKills(kills);
            est.setAssists(assists);
            est.setDeaths(deaths);
            est.setPlayer(p);
            est.setTeam(team);
            soma.add(est);
            kills = 0;
            deaths = 0;
            assists = 0;
            est = new Estatisticas();
        }
        return soma;

    }

    public List<Estatisticas> somaEstsPlayer(Player player) {
        List<Estatisticas> soma = new ArrayList<>();
        Estatisticas est = new Estatisticas();
        Integer kills = 0;
        Integer deaths = 0;
        Integer assists = 0;
        List<Estatisticas> ests = estatisticasServico.estatisticaPorPartidaPlayer(player.getId(), this.partida.getId());
        for (Estatisticas e : ests) {
            if (e.getPlayer().getId().equals(player.getId())) {
                kills += e.getKills();
                deaths += e.getDeaths();
                assists += e.getAssists();

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

        return soma;

    }

    public Partida getPartida() {
        return partida;
    }

    public void setPartida(Partida partida) {
        this.partida = partida;
    }

    public List<Player> getPlayersTime1() {
        return playersTime1;
    }

    public void setPlayersTime1(List<Player> playersTime1) {
        this.playersTime1 = playersTime1;
    }

    public List<Player> getPlayersTime2() {
        return playersTime2;
    }

    public void setPlayersTime2(List<Player> playersTime2) {
        this.playersTime2 = playersTime2;
    }

    public List<Partida> getPartidas() {
        return partidas;
    }

    public void setPartidas(List<Partida> partidas) {
        this.partidas = partidas;
    }

    public Partida getPartidaPesquisar() {
        return partidaPesquisar;
    }

    public void setPartidaPesquisar(Partida partidaPesquisar) {
        this.partidaPesquisar = partidaPesquisar;
    }

    public void pesquisar() {
        this.partidas = partidaServico.pesquisarPartidas(this.partidaPesquisar);
    }

    public List<ItemPartida> getItensPartidas() {
        return itensPartidas;
    }

    public void setItensPartidas(List<ItemPartida> itensPartidas) {
        this.itensPartidas = itensPartidas;
    }

    public ItemPartida getItemPartida() {
        return itemPartida;
    }

    public void setItemPartida(ItemPartida itemPartida) {
        this.itemPartida = itemPartida;
    }

    public int getQtdItensPartidas() {
        return qtdItensPartidas;
    }

    public void setQtdItensPartidas(int qtdItensPartidas) {
        this.qtdItensPartidas = qtdItensPartidas;
    }

    public String getCapitaoTime1() {
        return capitaoTime1;
    }

    public void setCapitaoTime1(String capitaoTime1) {
        this.capitaoTime1 = capitaoTime1;
    }

    public String getCapitaoTime2() {
        return capitaoTime2;
    }

    public void setCapitaoTime2(String capitaoTime2) {
        this.capitaoTime2 = capitaoTime2;
    }

    public String getNomeTime1() {
        return nomeTime1;
    }

    public void setNomeTime1(String nomeTime1) {
        this.nomeTime1 = nomeTime1;
    }

    public String getNomeTime2() {
        return nomeTime2;
    }

    public void setNomeTime2(String nomeTime2) {
        this.nomeTime2 = nomeTime2;
    }

    public List<ItemPartida> getItemPartidas() {
        return itemPartidas;
    }

    public void setItemPartidas(List<ItemPartida> itemPartidas) {
        this.itemPartidas = itemPartidas;
    }

    public List<Player> getAllPlayers() {
        return allPlayers;
    }

    public void setAllPlayers(List<Player> allPlayers) {
        this.allPlayers = allPlayers;
    }

    public List<Player> getSelectedPlayers() {
        return selectedPlayers;
    }

    public void setSelectedPlayers(List<Player> selectedPlayers) {
        this.selectedPlayers = selectedPlayers;
    }

    public DualListModel<Player> getPlayerGroupList() {
        return playerGroupList;
    }

    public void setPlayerGroupList(DualListModel<Player> playerGroupList) {
        this.playerGroupList = playerGroupList;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public void setPlayers(List<Player> players) {
        this.players = players;
    }

    public boolean isSkip() {
        return skip;
    }

    public void setSkip(boolean skip) {
        this.skip = skip;
    }

    public List<Estatisticas> getEstsGerais() {
        return estsGerais;
    }

    public void setEstsGerais(List<Estatisticas> estsGerais) {
        this.estsGerais = estsGerais;
    }

    public List<Mapas> getMapas() {
        return mapas;
    }

    public void setMapas(List<Mapas> mapas) {
        this.mapas = mapas;
    }

    public List<String> getPicksbans() {
        return picksbans;
    }

    public void setPicksbans(List<String> picksbans) {
        this.picksbans = picksbans;
    }

    public List<PickBanVo> getPickBanVo() {
        return pickBanVo;
    }

    public void setPickBanVo(List<PickBanVo> pickBanVo) {
        this.pickBanVo = pickBanVo;
    }

    public List<Mapas> getPickedMaps() {
        return pickedMaps;
    }

    public void setPickedMaps(List<Mapas> pickedMaps) {
        this.pickedMaps = pickedMaps;
    }

    public PickBanVo getPbItem() {
        return pbItem;
    }

    public void setPbItem(PickBanVo pbItem) {
        this.pbItem = pbItem;
    }

    public void limpar() throws Exception {
        instanciar();
    }

    public boolean isClassica() {
        return classica;
    }

    public void setClassica(boolean classica) {
        this.classica = classica;
    }

    public int getTipoEscolhaCapitaes() {
        return tipoEscolhaCapitaes;
    }

    public void setTipoEscolhaCapitaes(int tipoEscolhaCapitaes) {
        this.tipoEscolhaCapitaes = tipoEscolhaCapitaes;
    }

    public List<Player> getDroppedPlayers1() {
        return droppedPlayers1;
    }

    public void setDroppedPlayers1(List<Player> droppedPlayers1) {
        this.droppedPlayers1 = droppedPlayers1;
    }

    public List<Player> getDroppedPlayers2() {
        return droppedPlayers2;
    }

    public void setDroppedPlayers2(List<Player> droppedPlayers2) {
        this.droppedPlayers2 = droppedPlayers2;
    }

    public Player getSelectedPlayer() {
        return selectedPlayer;
    }

    public void setSelectedPlayer(Player selectedPlayer) {
        this.selectedPlayer = selectedPlayer;
    }

    public List<Player> getPickedPlayers() {
        return pickedPlayers;
    }

    public void setPickedPlayers(List<Player> pickedPlayers) {
        this.pickedPlayers = pickedPlayers;
    }

    public void excluir() {

    }

    public void pickarMapa(Mapas mapa) {
        if (pbItem.getTipoPickBan().getNome().equalsIgnoreCase("PICK")) {
            this.pickedMaps.add(mapa);
        }
        pbItem.setMapas(mapa);
        this.mapas.remove(mapa);

    }

    public void finalizarPicks() {
        try {
            List<ItemPartida> itensAtualizados = new ArrayList<>();
            itensAtualizados = PickBanUtils.setarMapas(this.pickedMaps, this.partida.getItemPartida());
            Partida p = new Partida();
            this.partida.setItemPartida(itensAtualizados);
            p = partidaServico.salvar(this.partida, this.partida.getId(), Url.ATUALIZAR_PARTIDA.getNome());
            Mensagem.successAndRedirect("Picks realizados com sucesso!", "visualizarPartida.xhtml?id=" + p.getId());
        } catch (Exception ex) {
            System.err.println(ex);
        }
    }

    public void retornarPartida() {
        Mensagem.successAndRedirect("Operação cancelada com sucesso!", "visualizarPartida.xhtml?id=" + this.partida.getId());
    }

    public void verificarPlayers() {
        this.selectedPlayers = this.playerGroupList.getTarget();
        if (this.selectedPlayers.size() % 2 == 0) {
            PrimeFaces.current().executeScript("PF('confirmarCriacaoX5Dialog').show();");
        } else {
            Mensagem.error("Para formar 2 times, adicione uma quantidade par de jogadores!");
            return;
        }
    }

    public void sorteioX5() {
        try {
            List<Player> time1 = new ArrayList<>();
            List<Player> time2 = new ArrayList<>();
            Team t1 = new Team();
            Team t2 = new Team();
            Partida partidaX5 = new Partida();
            List<Estatisticas> estsTeam1 = new ArrayList<Estatisticas>();
            List<Estatisticas> estsTeam2 = new ArrayList<Estatisticas>();

            this.selectedPlayers = this.playerGroupList.getTarget();

            Collections.shuffle(this.selectedPlayers);
            for (int i = 0; i < this.selectedPlayers.size() / 2; i++) {
                this.selectedPlayers.get(i).setPossuiTime(true);
                time1.add(this.selectedPlayers.get(i));
            }

            for (Player p : this.selectedPlayers) {
                if (!p.isPossuiTime()) {
                    time2.add(p);
                }

            }

            Team team1 = new Team();
            t1.setActive(true);
            t1.setNome(nomeTime1);
            t1.setPlayers(time1);
            t1.setTimeAmistoso(true);
            team1 = teamServico.save(t1, null, Url.SALVAR_TIME.getNome());

            Team team2 = new Team();
            t2.setActive(true);
            t2.setNome(nomeTime2);
            t2.setPlayers(time2);
            t2.setTimeAmistoso(true);
            team2 = teamServico.save(t2, null, Url.SALVAR_TIME.getNome());

            this.itemPartidas = PartidaUtils.gerarPartidasTimes(partidaX5, null, team1, team2, this.qtdItensPartidas);
            partidaX5.setItemPartida(this.itemPartidas);
            partida = partidaServico.salvar(partidaX5, null, Url.SALVAR_PARTIDA.getNome());
            List<ItemPartida> it = partida.getItemPartida();

            List<ItemPartida> newItem = new ArrayList<>();

            for (ItemPartida ip : partida.getItemPartida()) {
                ip.setPartida(partida.getId());
                newItem.add(ip);
            }

            partida.setItemPartida(newItem);

            partida = partidaServico.salvar(partida, partida.getId(), Url.ATUALIZAR_PARTIDA.getNome());

            for (ItemPartida i : it) {
                for (Player playerTime1 : team1.getPlayers()) {
                    Estatisticas estatisticas = new Estatisticas();
                    estatisticas.setPlayer(playerTime1);
                    estatisticas.setTeam(team1);
                    estatisticas.setItemPartida(i);
                    estsTeam1.add(estatisticas);

                }
                this.estsGerais.addAll(estsTeam1);

                for (Player playerTime2 : team2.getPlayers()) {
                    Estatisticas estatisticas = new Estatisticas();
                    estatisticas.setPlayer(playerTime2);
                    estatisticas.setTeam(team2);
                    estatisticas.setItemPartida(i);
                    estsTeam2.add(estatisticas);
                }
                this.estsGerais.addAll(estsTeam2);

                for (Estatisticas e : this.estsGerais) {
                    estatisticasServico.salvar(e, null, Url.SALVAR_ESTATISTICA.getNome());
                }

                estsTeam1 = new ArrayList<Estatisticas>();
                estsTeam2 = new ArrayList<Estatisticas>();
                this.estsGerais = new ArrayList<Estatisticas>();

            }

            Mensagem.successAndRedirect("Partida criada com sucesso", "visualizarPartida.xhtml?id=" + partida.getId());
        } catch (Exception ex) {
            System.err.println(ex);
        }
    }

    public void redirecionarPartida(ItemPartida item) {
        Mensagem.successAndRedirect("editarItem.xhtml?id=" + item.getId());
    }

    public void onPlayerDrop1(DragDropEvent<Player> ddEvent) {
        Player player = ddEvent.getData();

        droppedPlayers1.add(player);
        this.pickedPlayers.remove(player);
    }

    public void onPlayerDrop2(DragDropEvent<Player> ddEvent) {
        Player player = ddEvent.getData();

        droppedPlayers2.add(player);
        this.pickedPlayers.remove(player);
    }

    public void selecionarPartidaClassica() {
        System.err.println("por aqui...");
        this.pickedPlayers = this.playerGroupList.getTarget();
        if (this.tipoEscolhaCapitaes == 1) {
            System.err.println("sorteio...");
            Collections.shuffle(this.pickedPlayers);
            droppedPlayers1.add(this.pickedPlayers.get(0));
            if (!this.pickedPlayers.isEmpty() && this.pickedPlayers.get(0).getNick() != null) {
                this.nomeTime1 = "Time " + this.pickedPlayers.get(0).getNick();
            } else {
                this.nomeTime1 = "Time " + this.pickedPlayers.get(0).getNome();
            }

            this.pickedPlayers.remove(0);
            droppedPlayers2.add(this.pickedPlayers.get(0));

            if (!this.pickedPlayers.isEmpty() && this.pickedPlayers.get(0).getNick() != null) {
                this.nomeTime2 = "Time " + this.pickedPlayers.get(0).getNick();
            } else {
                this.nomeTime2 = "Time " + this.pickedPlayers.get(0).getNome();
            }
            this.pickedPlayers.remove(0);
        }
        PrimeFaces.current().executeScript("PF('selecaoDeCapitaesDialog').hide();");
    }

    public void dialogPartidaClassica() {
        if (this.tipoEscolhaCapitaes == 2) {
            this.nomeTime1 = "Time " + droppedPlayers1.get(0).getNick();
            this.nomeTime2 = "Time " + droppedPlayers2.get(0).getNick();
        }

        PrimeFaces.current().executeScript("PF('confirmarCriacaoX5Dialog').show();");
    }

    public void salvarPartida() {

        try {
            System.out.println("salvando...");
            System.out.println(this.partida.getDataPartida());
            partida = partidaServico.salvar(partida, null, Url.SALVAR_PARTIDA.getNome());
            Mensagem.successAndRedirect("Partida criada com sucesso", "partida-futebol-view.xhtml?id=" + partida.getId());

        } catch (Exception e) {

        }

    }

    public void salvarPartidaClassica() {
        try {

            Team t1 = new Team();
            Team t2 = new Team();
            Partida partidaX5 = new Partida();

            List<Estatisticas> estsTeam1 = new ArrayList<Estatisticas>();
            List<Estatisticas> estsTeam2 = new ArrayList<Estatisticas>();

            Team team1 = new Team();
            t1.setActive(true);
            t1.setNome(nomeTime1);
            t1.setPlayers(droppedPlayers1);
            team1 = teamServico.save(t1, null, Url.SALVAR_TIME.getNome());

            Team team2 = new Team();
            t2.setActive(true);
            t2.setNome(nomeTime2);
            t2.setPlayers(droppedPlayers2);
            team2 = teamServico.save(t2, null, Url.SALVAR_TIME.getNome());

            this.itemPartidas = PartidaUtils.gerarPartidasTimes(partidaX5, null, team1, team2, this.qtdItensPartidas);
            partidaX5.setItemPartida(this.itemPartidas);
            partida = partidaServico.salvar(partidaX5, null, Url.SALVAR_PARTIDA.getNome());
            List<ItemPartida> it = partida.getItemPartida();

            List<ItemPartida> newItem = new ArrayList<>();

            for (ItemPartida ip : partida.getItemPartida()) {
                ip.setPartida(partida.getId());
                newItem.add(ip);
            }

            partida.setItemPartida(newItem);

            partida = partidaServico.salvar(partida, partida.getId(), Url.ATUALIZAR_PARTIDA.getNome());

            for (ItemPartida i : it) {
                for (Player playerTime1 : team1.getPlayers()) {
                    Estatisticas estatisticas = new Estatisticas();
                    estatisticas.setPlayer(playerTime1);
                    estatisticas.setTeam(team1);
                    estatisticas.setItemPartida(i);
                    estsTeam1.add(estatisticas);

                }
                this.estsGerais.addAll(estsTeam1);

                for (Player playerTime2 : team2.getPlayers()) {
                    Estatisticas estatisticas = new Estatisticas();
                    estatisticas.setPlayer(playerTime2);
                    estatisticas.setTeam(team2);
                    estatisticas.setItemPartida(i);
                    estsTeam2.add(estatisticas);
                }
                this.estsGerais.addAll(estsTeam2);

                for (Estatisticas e : this.estsGerais) {
                    estatisticasServico.salvar(e, null, Url.SALVAR_ESTATISTICA.getNome());
                }

                estsTeam1 = new ArrayList<Estatisticas>();
                estsTeam2 = new ArrayList<Estatisticas>();
                this.estsGerais = new ArrayList<Estatisticas>();

            }

            Mensagem.successAndRedirect("Partida criada com sucesso", "visualizarPartida.xhtml?id=" + partida.getId());
        } catch (Exception ex) {
            System.err.println(ex);
        }
    }

    public void finalizarPartida() {
        try {
            List<Estatisticas> estsAux = new ArrayList<>();
            if (scoreT1 > scoreT2) {
                this.partida.setTimeVencedor(this.partida.getItemPartida().get(0).getTeam1());
                List<Estatisticas> ests = estatisticasServico.estatisticaPorPartidaTeam(this.partida.getItemPartida().get(0).getTeam1().getId(), this.partida.getId());
                System.err.println("size1 " + ests.size());
                for (Estatisticas e : ests) {
                    int pontos = e.getPontos();
                    e.setPontos(pontos + 3);
                    estsAux.add(e);
                    break;
                }
            } else {
                this.partida.setTimeVencedor(this.partida.getItemPartida().get(0).getTeam2());
                List<Estatisticas> ests = estatisticasServico.estatisticaPorPartidaTeam(this.partida.getItemPartida().get(0).getTeam2().getId(), this.partida.getId());
                System.err.println("size2 " + ests.size());
                for (Estatisticas e : ests) {
                    e.setPontos(e.getPontos() + 3);
                    estsAux.add(e);
                    break;
                }
            }
            for (Estatisticas e1 : estsAux) {
                estatisticasServico.salvar(e1, e1.getId(), Url.ATUALIZAR_ESTATISTICA.getNome());
            }
            this.partida.setFinalizada(true);
            this.partida = partidaServico.salvar(this.partida, this.partida.getId(), Url.ATUALIZAR_PARTIDA.getNome());
            Mensagem.successAndRedirect("Partida atualizada com sucesso", "visualizarPartida.xhtml?id=" + partida.getId());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public String retornaTimeVencedor() {
        if (scoreT1 > scoreT2) {
            return this.partida.getItemPartida().get(0).getTeam1().getNome();
        } else {
            return this.partida.getItemPartida().get(0).getTeam2().getNome();
        }
    }

    public boolean isEdicao() {
        return edicao;
    }

    public void setEdicao(boolean edicao) {
        this.edicao = edicao;
    }

    public String getHorarioPartida() {
        return horarioPartida;
    }

    public void setHorarioPartida(String horarioPartida) {
        this.horarioPartida = horarioPartida;
    }

    public int getTotalCheckins() {
        return totalCheckins;
    }

    public void setTotalCheckins(int totalCheckins) {
        this.totalCheckins = totalCheckins;
    }

    public void checkin() {

        try {
            System.out.println("realizando checkin...");
            if (this.playerLogado != null && !this.partida.getPlayers().contains(this.playerLogado)) {
                this.partida.getPlayers().add(this.playerLogado);
                this.partida = partidaServico.salvar(this.partida, this.partida.getId(), Url.ATUALIZAR_PARTIDA.getNome());
                Mensagem.successAndRedirect("Check-in realizado com sucesso!", "partida-futebol-view.xhtml?id=" + this.partida.getId());
            } else {
                PrimeFaces.current().ajax().update("@form");

            }
        } catch (Exception ex) {
            Logger.getLogger(ManagerPartida.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void removerParticipante(Player playerRemove) {

        try {

            this.partida.getPlayers().remove(playerRemove);
            this.partida = partidaServico.salvar(this.partida, this.partida.getId(), Url.ATUALIZAR_PARTIDA.getNome());
            Mensagem.successAndRedirect("Player removido com sucesso!", "partida-futebol-view.xhtml?id=" + this.partida.getId());

        } catch (Exception ex) {
            Logger.getLogger(ManagerPartida.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void checkout() {

    }

    public boolean isJaFezCheckin() {
        return jaFezCheckin;
    }

    public void setJaFezCheckin(boolean jaFezCheckin) {
        this.jaFezCheckin = jaFezCheckin;
    }

    public boolean isPodeParticipar() {
        return podeParticipar;
    }

    public void setPodeParticipar(boolean podeParticipar) {
        this.podeParticipar = podeParticipar;
    }

    public void gerarTimesFutebol() {

    }

    public String getHoraFormatadaPartida() {
        return horaFormatadaPartida;
    }

    public void setHoraFormatadaPartida(String horaFormatadaPartida) {
        this.horaFormatadaPartida = horaFormatadaPartida;
    }

    public int getPercentualParticipantes() {
        return percentualParticipantes;
    }

    public void setPercentualParticipantes(int percentualParticipantes) {
        this.percentualParticipantes = percentualParticipantes;
    }

    public String getTempoRestante() {
        return tempoRestante;
    }

    public void setTempoRestante(String tempoRestante) {
        this.tempoRestante = tempoRestante;
    }

    public BigDecimal getValorFormatado() {
        return valorFormatado;
    }

    public void setValorFormatado(BigDecimal valorFormatado) {
        this.valorFormatado = valorFormatado;
    }

    public int quantidadeJogadores() {
        int quantidade = 0;
        if (this.partida != null && this.partida.getPlayers() != null && !this.partida.getPlayers().isEmpty()) {
            quantidade = this.partida.getPlayers().size();
            return quantidade;
        }
        return quantidade;
    }

    public Player getPlayerLogado() {
        return playerLogado;
    }

    public void setPlayerLogado(Player playerLogado) {
        this.playerLogado = playerLogado;
    }

    public String getDataFormatadaPartida() {
        return dataFormatadaPartida;
    }

    public void setDataFormatadaPartida(String dataFormatadaPartida) {
        this.dataFormatadaPartida = dataFormatadaPartida;
    }

    public boolean playerPartida() {
        if (this.partida != null && this.partida.getPlayers() != null) {
            if (this.partida.getPlayers().size() > 0) {
                return true;
            }
        }
        return false;
    }

    public Team getTimeIniciante() {
        return timeIniciante;
    }

    public void setTimeIniciante(Team timeIniciante) {
        this.timeIniciante = timeIniciante;
    }
    
    public void sortearTimeIniciante(Team team){
        System.err.println("sorteou..." + team.getId());
        this.timeIniciante = team;
        this.pickBanVo = PickBanUtils.gerarListaPB(this.partida.getItemPartida().get(0).getTeam1(), this.partida.getItemPartida().get(0).getTeam2(), this.partida.getItemPartida().size(), this.timeIniciante);
    }
    

}
