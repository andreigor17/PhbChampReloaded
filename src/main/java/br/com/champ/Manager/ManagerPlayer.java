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
import br.com.champ.Modelo.Jogo;
import br.com.champ.Modelo.Player;
import br.com.champ.Servico.AnexoServico;
import br.com.champ.Servico.CampeonatoServico;
import br.com.champ.Servico.EstatisticaServico;
import br.com.champ.Servico.ItemPartidaServico;
import br.com.champ.Servico.PartidaServico;
import br.com.champ.Servico.PlayerServico;
import br.com.champ.Servico.TeamServico;
import br.com.champ.Utilitario.FacesUtil;
import br.com.champ.Utilitario.Mensagem;
import br.com.champ.Utilitario.Utils;
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
import org.primefaces.model.DualListModel;

/**
 *
 * @author andre
 */
@ViewScoped
@Named
public class ManagerPlayer implements Serializable {

    @EJB
    PlayerServico playerServico;
    @EJB
    CampeonatoServico campServico;
    @EJB
    AnexoServico anexoServico;
    @EJB
    TeamServico teamServico;
    @EJB
    PartidaServico partidaServico;
    @EJB
    ItemPartidaServico itemPartidaServico;
    @EJB
    EstatisticaServico estatisticaServico;
    private Player player;
    private List<Player> players;
    private List<Campeonato> camps;
    private String fotoDoPlayer;
    private List<Player> allPlayers;
    private List<Player> selectedPlayers;
    private DualListModel<Player> playerGroupList;
    private String capitaoTime1;
    private String capitaoTime2;
    private String nomeTime1;
    private String nomeTime2;
    private List<ItemPartida> itemPartidas;
    private ItemPartida itemPartida;
    private Estatisticas estatisticas;
    private List<Estatisticas> estsTotais;
    private List<Estatisticas> estsPlayer;
    private Jogo jogo;

    @PostConstruct
    public void init() {
        try {
            instanciar();

            String visualizarPlayerId = FacesUtil
                    .getRequestParameter("id");

            if (visualizarPlayerId != null && !visualizarPlayerId.isEmpty()) {
                this.player = this.playerServico.buscaPlayer(Long.parseLong(visualizarPlayerId));
            }

            if (this.player.getId() != null) {
                delegar();
            } else {
                instanciar();
            }
            HttpServletRequest uri = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();

            if (uri.getRequestURI().contains("sorteiox5.xhtml")) {
                instanciarPlayerGroup();
            }

        } catch (Exception ex) {
            Logger.getLogger(ManagerPlayer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void instanciar() throws Exception {
        this.player = new Player();
        this.players = new ArrayList<>();
        this.camps = new ArrayList<>();
        this.selectedPlayers = new ArrayList<>();
        this.itemPartidas = new ArrayList<>();
        this.itemPartida = new ItemPartida();
        this.estatisticas = new Estatisticas();
        this.estsTotais = new ArrayList<>();
        this.estsPlayer = new ArrayList<>();
        this.jogo = new Jogo();
    }

    public void instanciarPlayerGroup() throws Exception {
        this.player = new Player();
        this.allPlayers = playerServico.pesquisar(this.player.getNome());
        this.playerGroupList = new DualListModel<>(this.allPlayers, this.selectedPlayers);
    }

    public void delegar() {
        this.camps = campServico.buscaCampPorPlayer(Url.CAMPEONATOS_PLAYERS.getNome(), this.player.getId(), this.jogo.getId());
    }

    public List<Estatisticas> somaEstsPayer() {
        List<Estatisticas> soma = new ArrayList<>();
        Estatisticas est = new Estatisticas();
        Integer kills = 0;
        Integer deaths = 0;
        Integer assists = 0;
        Integer pontos = 0;

        if (Utils.isNotEmpty(this.camps)) {
            for (Campeonato camp : this.camps) {
                this.estsPlayer = estatisticaServico.estatisticaPorPlayer(this.player.getId(), camp.getId());
                for (Estatisticas estats : this.estsPlayer) {
                    if (estats.getPlayer().getId().equals(this.player.getId())) {
                        kills += estats.getKills();
                        deaths += estats.getDeaths();
                        assists += estats.getAssists();
                    }

                }

                est.setKills(kills);
                est.setAssists(assists);
                est.setDeaths(deaths);
                est.setPlayer(this.player);
                soma.add(est);
                kills = 0;
                deaths = 0;
                assists = 0;
                est = new Estatisticas();
            }
        }
        return soma;

    }

    public List<Estatisticas> getEstsPlayer() {
        return estsPlayer;
    }

    public void setEstsPlayer(List<Estatisticas> estsPlayer) {
        this.estsPlayer = estsPlayer;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public Estatisticas getEstatisticas() {
        return estatisticas;
    }

    public void setEstatisticas(Estatisticas estatisticas) {
        this.estatisticas = estatisticas;
    }

    public List<Estatisticas> getEstsTotais() {
        return estsTotais;
    }

    public void setEstsTotais(List<Estatisticas> estsTotais) {
        this.estsTotais = estsTotais;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public void setPlayers(List<Player> players) {
        this.players = players;
    }

    public List<Campeonato> getCamps() {
        return camps;
    }

    public void setCamps(List<Campeonato> camps) {
        this.camps = camps;
    }

    public String getFotoDoPlayer() {
        return fotoDoPlayer;
    }

    public void setFotoDoPlayer(String fotoDoPlayer) {
        this.fotoDoPlayer = fotoDoPlayer;
    }

    public DualListModel<Player> getPlayerGroupList() {
        return playerGroupList;
    }

    public void setPlayerGroupList(DualListModel<Player> playerGroupList) {
        this.playerGroupList = playerGroupList;
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

    public ItemPartida getItemPartida() {
        return itemPartida;
    }

    public void setItemPartida(ItemPartida itemPartida) {
        this.itemPartida = itemPartida;
    }

    public void salvarPlayer() {
        //this.playerServico.salvar(this.player);
        //Mensagem.successAndRedirect("Player salvo com sucesso", "visualizarPlayer.xhtml?id=" + this.player.getId());
    }

    public void pesquisarPlayer() throws Exception {
        this.players = playerServico.pesquisar(this.player.getNome());
    }

    public void limpar() throws Exception {
        instanciar();
    }

    public void removerPlayer() throws Exception {
        //this.playerServico.delete(this.player);
        Mensagem.successAndRedirect("pesquisarPlayer.xhtml");
        init();
    }

}
