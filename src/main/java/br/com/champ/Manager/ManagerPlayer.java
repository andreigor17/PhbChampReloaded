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
import br.com.champ.Servico.SteamAPIServico;
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
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.primefaces.PrimeFaces;
import org.primefaces.model.DualListModel;

/**
 *
 * @author andre
 */
@ViewScoped
@Named
public class ManagerPlayer extends ManagerBase {

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
    @EJB
    SteamAPIServico steamApiService;
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
    private String steamId64;
    private JSONObject profileData;
    private int steamLevel;
    private int gameCount;
    private int friendCount;
    private double totalPlaytime;
    private String profileUrl;
    private String avatarUrl;
    private boolean profileLoaded;
    private String statusConta;
    private boolean error;
    private boolean loading;
    private String termoBusca;
    private boolean buscando;
    private boolean estadoInicial = true;
    private Player playerSelecionado;
    private Integer ratingTemp;

    @PostConstruct
    public void init() {
        try {
            instanciar();
            // Carrega o player logado através do ManagerBase
            getPlayerLogado();
            HttpServletRequest uri = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();

            String visualizarPlayerId = FacesUtil
                    .getRequestParameter("id");

            if (visualizarPlayerId != null && !visualizarPlayerId.isEmpty()) {
                this.player = this.playerServico.buscaPlayer(Long.parseLong(visualizarPlayerId));
                if (!Utils.isEmpty(this.player.getSteamID())) {
                    this.loading = true;
                }
            }

            if (this.player.getId() != null) {
                delegar();
            } else {
                instanciar();
            }

            if (this.player != null && this.player.getRating() != null) {
                this.ratingTemp = this.player.getRating().intValue();
            } else {
                this.ratingTemp = null;
            }

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
        this.playerSelecionado = new Player();
        this.ratingTemp = null;
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
        // Se o termo de busca for null, vazio ou menor que 2 caracteres, limpa a busca
        if (termoBusca == null || termoBusca.trim().length() < 2) {
            limparBusca();
            return;
        }

        estadoInicial = false;
        buscando = true;

        try {
            // Chama o serviço para pesquisar
            List<Player> resultado = playerServico.pesquisar(this.termoBusca.trim());
            
            // Garante que sempre temos uma lista (nunca null)
            if (resultado == null) {
                this.players = new ArrayList<>();
            } else {
                this.players = resultado;
            }
        } catch (Exception e) {
            System.err.println("Erro ao pesquisar player: " + e.getMessage());
            e.printStackTrace();
            // Em caso de erro, inicializa lista vazia
            this.players = new ArrayList<>();
        } finally {
            buscando = false;
        }
    }

    public void limparBusca() {
        termoBusca = "";
        this.players = new ArrayList<>();
        estadoInicial = true;
        buscando = false;
    }

    public void limpar() throws Exception {
        instanciar();
    }

    public void removerPlayer() throws Exception {
        // Validação de segurança: apenas admin pode excluir
        if (!isUsuarioLogado() || !isAdmin()) {
            Mensagem.warn("Apenas administradores podem excluir jogadores.");
            return;
        }
        
        this.playerServico.delete(this.player, Url.APAGAR_PLAYER.getNome());
        Mensagem.successAndRedirect("Jogador excluído com sucesso!", "jogadores.xhtml");
        init();
    }

    public void salvarRating() {
        if (!isAdmin()) {
            Mensagem.warn("Somente administradores podem avaliar jogadores.");
            return;
        }

        if (this.ratingTemp == null || this.ratingTemp < 1 || this.ratingTemp > 10) {
            Mensagem.warn("Informe um rating entre 1 e 10.");
            return;
        }

        try {
            this.player.setRating(this.ratingTemp.doubleValue());
            Player atualizado = playerServico.save(this.player, this.player.getId(), Url.ATUALIZAR_PLAYER.getNome());

            if (atualizado != null) {
                this.player = atualizado;
                this.ratingTemp = atualizado.getRating() != null ? atualizado.getRating().intValue() : null;
                PrimeFaces.current().ajax().update("playerForm:playerRatingPanel");
                Mensagem.success("Rating atualizado com sucesso!");
            } else {
                Mensagem.error("Não foi possível atualizar o rating do jogador.");
            }
        } catch (Exception e) {
            Mensagem.error("Erro ao atualizar o rating do jogador.");
            e.printStackTrace();
        }
    }

    /**
     * Verifica se o usuário logado tem permissão para editar/excluir este player
     * Permite se for admin ou se for o próprio dono do perfil
     */
    public boolean verificarPermissao() {
        return this.player != null && this.player.getId() != null 
                && podeEditar(this.player.getId());
    }

    public void carregarDadosRemote() {
        System.err.println("chegou do remote...");
        this.profileLoaded = false;
        this.loading = true;
        if (this.player != null && this.player.getId() != null && this.player.getSteamID() != null && !this.player.getSteamID().isEmpty()) {
            carregarDadosSteam();
        }
    }

    public void carregarDadosSteam() {
        try {

            steamId64 = steamApiService.convertSteamIdTo64(this.player.getSteamID());

            profileData = steamApiService.getPlayerProfile(steamId64);

            if (!profileData.isEmpty()) {
                profileUrl = profileData.getString("profileurl");
                avatarUrl = profileData.getString("avatarfull");

                int visibilityState = profileData.getInt("communityvisibilitystate");
                statusConta = (visibilityState == 3) ? "Pública" : "Privada";

                steamLevel = steamApiService.getSteamLevel(steamId64);

                JSONObject gamesData = steamApiService.getOwnedGames(steamId64);
                gameCount = gamesData.getJSONObject("response").getInt("game_count");

                JSONArray friendsData = steamApiService.getFriendsList(steamId64);
                friendCount = friendsData.length();

                totalPlaytime = steamApiService.getTotalPlaytime(steamId64);

                this.profileLoaded = true;
                this.loading = false;
                System.err.println("carrgado " + this.profileLoaded);
                PrimeFaces.current().executeScript("atualizarCardSteam();");
            } else {
                this.error = true;
            }
        } catch (Exception e) {
            // Tratar erro
            e.printStackTrace();
        }
    }

    public String getFormattedPlaytime() {
        int hours = (int) totalPlaytime;
        return hours + "h";
    }

    public Jogo getJogo() {
        return jogo;
    }

    public void setJogo(Jogo jogo) {
        this.jogo = jogo;
    }

    public String getSteamId64() {
        return steamId64;
    }

    public void setSteamId64(String steamId64) {
        this.steamId64 = steamId64;
    }

    public JSONObject getProfileData() {
        return profileData;
    }

    public void setProfileData(JSONObject profileData) {
        this.profileData = profileData;
    }

    public int getSteamLevel() {
        return steamLevel;
    }

    public void setSteamLevel(int steamLevel) {
        this.steamLevel = steamLevel;
    }

    public int getGameCount() {
        return gameCount;
    }

    public void setGameCount(int gameCount) {
        this.gameCount = gameCount;
    }

    public int getFriendCount() {
        return friendCount;
    }

    public void setFriendCount(int friendCount) {
        this.friendCount = friendCount;
    }

    public double getTotalPlaytime() {
        return totalPlaytime;
    }

    public void setTotalPlaytime(double totalPlaytime) {
        this.totalPlaytime = totalPlaytime;
    }

    public String getProfileUrl() {
        return profileUrl;
    }

    public void setProfileUrl(String profileUrl) {
        this.profileUrl = profileUrl;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public boolean isProfileLoaded() {
        return profileLoaded;
    }

    public void setProfileLoaded(boolean profileLoaded) {
        this.profileLoaded = profileLoaded;
    }

    public String getStatusConta() {
        return statusConta;
    }

    public void setStatusConta(String statusConta) {
        this.statusConta = statusConta;
    }

    public boolean isError() {
        return error;
    }

    public void setError(boolean error) {
        this.error = error;
    }

    public boolean isLoading() {
        return loading;
    }

    public void setLoading(boolean loading) {
        this.loading = loading;
    }

    public String getTermoBusca() {
        return termoBusca;
    }

    public void setTermoBusca(String termoBusca) {
        this.termoBusca = termoBusca;
    }

    public boolean isBuscando() {
        return buscando;
    }

    public void setBuscando(boolean buscando) {
        this.buscando = buscando;
    }

    public boolean isEstadoInicial() {
        return estadoInicial;
    }

    public void setEstadoInicial(boolean estadoInicial) {
        this.estadoInicial = estadoInicial;
    }

    public boolean isTemResultados() {
        return !estadoInicial && !buscando && this.players != null && !this.players.isEmpty();
    }

    public boolean isSemResultados() {
        return !estadoInicial && !buscando && (this.players == null || this.players.isEmpty());
    }

    public Player getPlayerSelecionado() {
        return playerSelecionado;
    }

    public void setPlayerSelecionado(Player playerSelecionado) {
        this.playerSelecionado = playerSelecionado;
    }

    public Integer getRatingTemp() {
        return ratingTemp;
    }

    public void setRatingTemp(Integer ratingTemp) {
        this.ratingTemp = ratingTemp;
    }

}
