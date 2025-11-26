/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.champ.Manager;

import br.com.champ.Enums.Url;
import br.com.champ.Modelo.Campeonato;
import br.com.champ.Modelo.Estatisticas;
import br.com.champ.Modelo.ItemPartida;
import br.com.champ.Modelo.Mapas;
import br.com.champ.Modelo.Partida;
import br.com.champ.Modelo.Player;
import br.com.champ.Modelo.Team;
import br.com.champ.Servico.CampeonatoServico;
import br.com.champ.Servico.EstatisticaServico;
import br.com.champ.Servico.ItemPartidaServico;
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
import jakarta.inject.Inject;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
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
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jakarta.faces.context.ExternalContext;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 *
 * @author andre
 */
@ViewScoped
@Named
public class ManagerPartida extends ManagerBase {

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
    CampeonatoServico campeonatoServico;
    @Inject
    private br.com.champ.Servico.RconService rconService;
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
    private Team timeIniciante;
    private List<Partida> historicoTime1;
    private List<Partida> historicoTime2;
    private Campeonato campeonato;

    @PostConstruct
    public void init() {
        try {
            instanciar();
            String visualizarPartidaId = FacesUtil
                    .getRequestParameter("id");
            String gerarMapasId = FacesUtil
                    .getRequestParameter("partidaId");

            // Carrega o player logado através do ManagerBase
            getPlayerLogado();

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
                    // Ordenar itensPartidas por ID
                    if (this.itensPartidas != null && !this.itensPartidas.isEmpty()) {
                        this.itensPartidas.sort((item1, item2) -> {
                            if (item1.getId() == null && item2.getId() == null) {
                                return 0;
                            }
                            if (item1.getId() == null) {
                                return 1;
                            }
                            if (item2.getId() == null) {
                                return -1;
                            }
                            return item1.getId().compareTo(item2.getId());
                        });
                    }
                    this.mapas = mapaServico.pesquisar();

                    gerarScore();
                    createRadarModel();
                    
                    // Carregar campeonato se houver no itemPartida
                    if (!this.itensPartidas.isEmpty() && this.itensPartidas.get(0).getCamp() != null) {
                        try {
                            this.campeonato = campeonatoServico.buscaCamp(this.itensPartidas.get(0).getCamp());
                        } catch (Exception ex) {
                            System.err.println("Erro ao carregar campeonato: " + ex);
                        }
                    }
                    
                    // Carregar histórico dos times
                    if (!this.itensPartidas.isEmpty() && this.itensPartidas.get(0).getTeam1() != null && this.itensPartidas.get(0).getTeam2() != null) {
                        this.historicoTime1 = partidaServico.pesquisarPartidasGeral();
                        this.historicoTime2 = partidaServico.pesquisarPartidasGeral();
                    }
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
        this.nomeTime1 = null;
        this.nomeTime2 = null;
        this.timeIniciante = null;
        this.historicoTime1 = new ArrayList<>();
        this.historicoTime2 = new ArrayList<>();
        this.campeonato = null;
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

    public void distribuirJogadores() {
        System.err.println("Distribuindo...");

        // Garante que a lista é mutável antes de embaralhar e remover
        this.pickedPlayers = new ArrayList<>(this.pickedPlayers);

        // Embaralha a lista de forma aleatória
        Collections.shuffle(this.pickedPlayers);

        int faltandoTime1 = 5 - droppedPlayers1.size();
        int faltandoTime2 = 5 - droppedPlayers2.size();

        // Preenche time 1
        for (int i = 0; i < faltandoTime1 && !this.pickedPlayers.isEmpty(); i++) {
            Player p = this.pickedPlayers.remove(0);
            droppedPlayers1.add(p);
        }

        // Preenche time 2
        for (int i = 0; i < faltandoTime2 && !this.pickedPlayers.isEmpty(); i++) {
            Player p = this.pickedPlayers.remove(0);
            droppedPlayers2.add(p);
        }

        System.out.println("Time 1: " + droppedPlayers1);
        System.out.println("Time 2: " + droppedPlayers2);
        System.out.println("Restantes (pickedPlayers): " + pickedPlayers);
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
                this.partida.setTimePerdedor(this.partida.getItemPartida().get(0).getTeam2());
//                List<Estatisticas> ests = estatisticasServico.estatisticaPorPartidaTeam(this.partida.getItemPartida().get(0).getTeam1().getId(), this.partida.getId());
//                System.err.println("size1 " + ests.size());
//                for (Estatisticas e : ests) {
//                    //int pontos = e.getPontos();
//                    //e.setPontos(pontos + 3);
//                    estsAux.add(e);
//                    break;
                //}
            } else {
                this.partida.setTimeVencedor(this.partida.getItemPartida().get(0).getTeam2());
                this.partida.setTimePerdedor(this.partida.getItemPartida().get(0).getTeam1());
//                List<Estatisticas> ests = estatisticasServico.estatisticaPorPartidaTeam(this.partida.getItemPartida().get(0).getTeam2().getId(), this.partida.getId());
//                System.err.println("size2 " + ests.size());
//                for (Estatisticas e : ests) {
//                    //e.setPontos(e.getPontos() + 3);
//                    estsAux.add(e);
//                    break;
//                }
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
            Player player = getPlayerLogado();
            if (player != null && !this.partida.getPlayers().contains(player)) {
                this.partida.getPlayers().add(player);
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

    public void sortearTimeIniciante(Team team) {
        System.err.println("sorteou..." + team.getId());
        this.timeIniciante = team;
        this.pickBanVo = PickBanUtils.gerarListaPB(this.partida.getItemPartida().get(0).getTeam1(), this.partida.getItemPartida().get(0).getTeam2(), this.partida.getItemPartida().size(), this.timeIniciante);
    }

    public List<Partida> getHistoricoTime1() {
        return historicoTime1;
    }

    public void setHistoricoTime1(List<Partida> historicoTime1) {
        this.historicoTime1 = historicoTime1;
    }

    public List<Partida> getHistoricoTime2() {
        return historicoTime2;
    }

    public void setHistoricoTime2(List<Partida> historicoTime2) {
        this.historicoTime2 = historicoTime2;
    }

    public Campeonato getCampeonato() {
        return campeonato;
    }

    public void setCampeonato(Campeonato campeonato) {
        this.campeonato = campeonato;
    }

    /**
     * Verifica se o usuário pode atualizar a partida.
     * Retorna true se:
     * - A partida ainda não estiver finalizada OU
     * - O usuário logado for admin OU
     * - O usuário logado for igual ao usuarioCadastro da partida
     * 
     * @return true se pode atualizar, false caso contrário
     */
    public boolean podeAtualizarPartida() {
        if (this.partida == null) {
            return false;
        }
        
        // Se a partida não está finalizada, pode atualizar
        if (!this.partida.isFinalizada()) {
            return true;
        }
        
        // Se é admin, pode atualizar
        if (isAdmin()) {
            return true;
        }
        
        // Se é o usuário que cadastrou a partida, pode atualizar
        Player playerLogado = getPlayerLogado();
        if (playerLogado != null && this.partida.getUsuarioCadastro() != null) {
            if (playerLogado.getId() != null && this.partida.getUsuarioCadastro().getId() != null) {
                return playerLogado.getId().equals(this.partida.getUsuarioCadastro().getId());
            }
        }
        
        return false;
    }

    /**
     * Retorna a URL de destino para o botão voltar.
     * Se houver campeonato, retorna para visualizarCampeonato.xhtml com o id do campeonato.
     * Caso contrário, retorna para indexPartida.xhtml
     * 
     * @return String com a outcome e parâmetros
     */
    public String getUrlVoltar() {
        if (this.campeonato != null && this.campeonato.getId() != null) {
            return "visualizarCampeonato.xhtml?id=" + this.campeonato.getId();
        }
        return "indexPartida.xhtml";
    }

    /**
     * Gera o JSON da partida no formato MatchZy e retorna como String
     * @return String com o JSON gerado
     */
    private String gerarJsonString() throws Exception {
        if (this.partida == null || this.itensPartidas == null || this.itensPartidas.isEmpty()) {
            throw new Exception("Partida não encontrada ou sem itens.");
        }

        ItemPartida primeiroItem = this.itensPartidas.get(0);
        
        // Verifica se é partida de times ou de players individuais
        if (primeiroItem.getTeam1() == null && primeiroItem.getPlayer1() == null) {
            throw new Exception("Partida inválida: sem times ou players definidos.");
        }

        // Usa LinkedHashMap para manter a ordem dos campos
        Map<String, Object> jsonData = new LinkedHashMap<>();
        
        // 1. matchid
        jsonData.put("matchid", this.partida.getId());
        
        // 2. team1
        Map<String, Object> team1Data = new LinkedHashMap<>();
        int playersPerTeam = 0;
        if (primeiroItem.getTeam1() != null) {
            Team team1 = primeiroItem.getTeam1();
            team1Data.put("name", team1.getNome());
            
            // Players do time 1
            Map<String, String> players1 = new LinkedHashMap<>();
            if (team1.getPlayers() != null) {
                for (Player player : team1.getPlayers()) {
                    String steamId = player.getSteamId64() != null && !player.getSteamId64().isEmpty() 
                        ? player.getSteamId64() 
                        : (player.getSteamID() != null ? player.getSteamID() : "");
                    if (steamId != null && !steamId.isEmpty() && player.getNick() != null) {
                        players1.put(steamId, player.getNick());
                    }
                }
                playersPerTeam = team1.getPlayers().size();
            }
            team1Data.put("players", players1);
        } else if (primeiroItem.getPlayer1() != null) {
            // Se for partida individual, usa o nome do player
            Player player1 = primeiroItem.getPlayer1();
            team1Data.put("name", player1.getNick() != null ? player1.getNick() : player1.getNome());
            
            Map<String, String> players1 = new LinkedHashMap<>();
            String steamId = player1.getSteamId64() != null && !player1.getSteamId64().isEmpty() 
                ? player1.getSteamId64() 
                : (player1.getSteamID() != null ? player1.getSteamID() : "");
            if (steamId != null && !steamId.isEmpty() && player1.getNick() != null) {
                players1.put(steamId, player1.getNick());
            }
            team1Data.put("players", players1);
            playersPerTeam = 1;
        }
        jsonData.put("team1", team1Data);
        
        // 3. team2
        Map<String, Object> team2Data = new LinkedHashMap<>();
        if (primeiroItem.getTeam2() != null) {
            Team team2 = primeiroItem.getTeam2();
            team2Data.put("name", team2.getNome());
            
            // Players do time 2
            Map<String, String> players2 = new LinkedHashMap<>();
            if (team2.getPlayers() != null) {
                for (Player player : team2.getPlayers()) {
                    String steamId = player.getSteamId64() != null && !player.getSteamId64().isEmpty() 
                        ? player.getSteamId64() 
                        : (player.getSteamID() != null ? player.getSteamID() : "");
                    if (steamId != null && !steamId.isEmpty() && player.getNick() != null) {
                        players2.put(steamId, player.getNick());
                    }
                }
                if (playersPerTeam == 0) {
                    playersPerTeam = team2.getPlayers().size();
                }
            }
            team2Data.put("players", players2);
        } else if (primeiroItem.getPlayer2() != null) {
            // Se for partida individual, usa o nome do player
            Player player2 = primeiroItem.getPlayer2();
            team2Data.put("name", player2.getNick() != null ? player2.getNick() : player2.getNome());
            
            Map<String, String> players2 = new LinkedHashMap<>();
            String steamId = player2.getSteamId64() != null && !player2.getSteamId64().isEmpty() 
                ? player2.getSteamId64() 
                : (player2.getSteamID() != null ? player2.getSteamID() : "");
            if (steamId != null && !steamId.isEmpty() && player2.getNick() != null) {
                players2.put(steamId, player2.getNick());
            }
            team2Data.put("players", players2);
            if (playersPerTeam == 0) {
                playersPerTeam = 1;
            }
        }
        jsonData.put("team2", team2Data);
        
        // 4. num_maps
        jsonData.put("num_maps", this.itensPartidas.size());
        
        // 5. maplist
        List<String> maplist = new ArrayList<>();
        for (ItemPartida item : this.itensPartidas) {
            if (item.getMapas() != null && item.getMapas().getNome() != null) {
                maplist.add(item.getMapas().getNome());
            }
        }
        jsonData.put("maplist", maplist);
        
        // 6. map_sides - alterna entre team1_ct, team2_ct, knife
        List<String> mapSides = new ArrayList<>();
        for (int i = 0; i < this.itensPartidas.size(); i++) {
            if (i == this.itensPartidas.size() - 1) {
                // Último mapa usa knife
                mapSides.add("knife");
            } else if (i % 2 == 0) {
                // Mapa par: team1_ct
                mapSides.add("team1_ct");
            } else {
                // Mapa ímpar: team2_ct
                mapSides.add("team2_ct");
            }
        }
        jsonData.put("map_sides", mapSides);
        
        // 7. spectators
        Map<String, Object> spectators = new LinkedHashMap<>();
        Map<String, String> spectatorPlayers = new LinkedHashMap<>();
        // Por enquanto vazio, pode ser preenchido no futuro se houver espectadores
        spectators.put("players", spectatorPlayers);
        jsonData.put("spectators", spectators);
        
        // 8. clinch_series
        jsonData.put("clinch_series", true);
        
        // 9. players_per_team
        jsonData.put("players_per_team", playersPerTeam);
        
        // 10. cvars
        Map<String, String> cvars = new LinkedHashMap<>();
        String nomeTime1 = "";
        String nomeTime2 = "";
        if (primeiroItem.getTeam1() != null) {
            nomeTime1 = primeiroItem.getTeam1().getNome() != null ? primeiroItem.getTeam1().getNome() : "Time1";
        } else if (primeiroItem.getPlayer1() != null) {
            nomeTime1 = primeiroItem.getPlayer1().getNick() != null ? primeiroItem.getPlayer1().getNick() 
                : (primeiroItem.getPlayer1().getNome() != null ? primeiroItem.getPlayer1().getNome() : "Player1");
        }
        if (primeiroItem.getTeam2() != null) {
            nomeTime2 = primeiroItem.getTeam2().getNome() != null ? primeiroItem.getTeam2().getNome() : "Time2";
        } else if (primeiroItem.getPlayer2() != null) {
            nomeTime2 = primeiroItem.getPlayer2().getNick() != null ? primeiroItem.getPlayer2().getNick() 
                : (primeiroItem.getPlayer2().getNome() != null ? primeiroItem.getPlayer2().getNome() : "Player2");
        }
        cvars.put("hostname", "MatchZy: " + nomeTime1 + " vs " + nomeTime2 + " #" + this.partida.getId());
        cvars.put("mp_friendlyfire", "0");
        jsonData.put("cvars", cvars);
        
        // Converte para JSON
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(jsonData);
    }
    
    /**
     * Gera o nome do arquivo no formato: nometime1_vs_nometime2_idpartida.json
     */
    private String gerarNomeArquivo() {
        ItemPartida primeiroItem = this.itensPartidas.get(0);
        String nomeTime1 = "";
        String nomeTime2 = "";
        
        if (primeiroItem.getTeam1() != null) {
            nomeTime1 = primeiroItem.getTeam1().getNome() != null ? primeiroItem.getTeam1().getNome() : "Time1";
        } else if (primeiroItem.getPlayer1() != null) {
            nomeTime1 = primeiroItem.getPlayer1().getNick() != null ? primeiroItem.getPlayer1().getNick() 
                : (primeiroItem.getPlayer1().getNome() != null ? primeiroItem.getPlayer1().getNome() : "Player1");
        }
        
        if (primeiroItem.getTeam2() != null) {
            nomeTime2 = primeiroItem.getTeam2().getNome() != null ? primeiroItem.getTeam2().getNome() : "Time2";
        } else if (primeiroItem.getPlayer2() != null) {
            nomeTime2 = primeiroItem.getPlayer2().getNick() != null ? primeiroItem.getPlayer2().getNick() 
                : (primeiroItem.getPlayer2().getNome() != null ? primeiroItem.getPlayer2().getNome() : "Player2");
        }
        
        // Remove caracteres especiais e espaços para o nome do arquivo
        nomeTime1 = nomeTime1.replaceAll("[^a-zA-Z0-9]", "_").replaceAll("_+", "_");
        nomeTime2 = nomeTime2.replaceAll("[^a-zA-Z0-9]", "_").replaceAll("_+", "_");
        
        return nomeTime1 + "_vs_" + nomeTime2 + "_" + this.partida.getId() + ".json";
    }
    
    /**
     * Gera e faz download do JSON da partida no formato MatchZy
     */
    public void baixarJsonPartida() {
        FacesContext facesContext = null;
        try {
            String jsonString = gerarJsonString();
            String nomeArquivo = gerarNomeArquivo();
            
            // Faz o download do arquivo
            facesContext = FacesContext.getCurrentInstance();
            if (facesContext == null || facesContext.getResponseComplete()) {
                return;
            }
            
            ExternalContext externalContext = facesContext.getExternalContext();
            
            externalContext.responseReset();
            externalContext.setResponseContentType("application/json");
            externalContext.setResponseCharacterEncoding("UTF-8");
            externalContext.setResponseHeader("Content-Disposition", 
                "attachment; filename=\"" + nomeArquivo + "\"");
            
            try (OutputStream outputStream = externalContext.getResponseOutputStream()) {
                outputStream.write(jsonString.getBytes(StandardCharsets.UTF_8));
                outputStream.flush();
            }
            
            facesContext.responseComplete();
            
        } catch (Exception ex) {
            Logger.getLogger(ManagerPartida.class.getName()).log(Level.SEVERE, null, ex);
            try {
                if (facesContext != null && !facesContext.getResponseComplete()) {
                    Mensagem.error("Erro ao baixar JSON da partida: " + ex.getMessage());
                }
            } catch (Exception e) {
                Logger.getLogger(ManagerPartida.class.getName()).log(Level.SEVERE, "Erro ao exibir mensagem de erro", e);
            }
        }
    }
    
    /**
     * Gera o JSON da partida e salva no servidor em /opt/cs/game/csgo/
     */
    public void gerarJsonPartida() {
        try {
            String jsonString = gerarJsonString();
            String nomeArquivo = gerarNomeArquivo();
            
            // Caminho do servidor
            String caminhoServidor = "/opt/cs/game/csgo/";
            File diretorio = new File(caminhoServidor);
            
            // Cria o diretório se não existir
            if (!diretorio.exists()) {
                diretorio.mkdirs();
            }
            
            // Cria o arquivo
            File arquivo = new File(diretorio, nomeArquivo);
            
            // Escreve o JSON no arquivo
            try (FileOutputStream fos = new FileOutputStream(arquivo)) {
                fos.write(jsonString.getBytes(StandardCharsets.UTF_8));
                fos.flush();
            }
            
            Mensagem.success("JSON gerado com sucesso em: " + caminhoServidor + nomeArquivo);
            
        } catch (Exception ex) {
            Logger.getLogger(ManagerPartida.class.getName()).log(Level.SEVERE, null, ex);
            Mensagem.error("Erro ao gerar JSON da partida: " + ex.getMessage());
        }
    }
    
    /**
     * Inicia a partida no servidor CS2 via RCON usando o arquivo JSON gerado
     */
    public void iniciarPartidaRcon() {
        try {
            // Verifica se o JSON foi gerado
            String nomeArquivo = gerarNomeArquivo();
            String caminhoServidor = "/opt/cs/game/csgo/";
            File arquivo = new File(caminhoServidor, nomeArquivo);
            
            if (!arquivo.exists()) {
                Mensagem.error("Arquivo JSON não encontrado. Gere o JSON primeiro antes de iniciar a partida.");
                return;
            }
            
            // Monta o comando RCON
            String comando = "matchzy_loadmatch " + nomeArquivo;
            
            // Executa o comando via RCON
            String resultado = rconService.executeCommand(comando);
            
            if (resultado != null && !resultado.startsWith("ERRO")) {
                Mensagem.success("Partida iniciada com sucesso! Comando: " + comando);
            } else {
                Mensagem.error("Erro ao iniciar partida via RCON: " + (resultado != null ? resultado : "Sem resposta do servidor"));
            }
            
        } catch (Exception ex) {
            Logger.getLogger(ManagerPartida.class.getName()).log(Level.SEVERE, null, ex);
            Mensagem.error("Erro ao iniciar partida: " + ex.getMessage());
        }
    }

}
