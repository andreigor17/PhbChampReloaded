/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.champ.Manager;

import br.com.champ.Enums.Url;
import br.com.champ.Modelo.Configuracao;
import br.com.champ.Modelo.Jogo;
import br.com.champ.Modelo.Player;
import br.com.champ.Servico.AnexoServico;
import br.com.champ.Servico.ConfiguracaoServico;
import br.com.champ.Servico.JogoServico;
import br.com.champ.Servico.PlayerServico;
import br.com.champ.Utilitario.FacesUtil;
import br.com.champ.Utilitario.Mensagem;
import br.com.champ.vo.PlayerSteamVo;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpServletRequest;
import java.io.Serializable;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.file.UploadedFile;

/**
 *
 * @author andre
 */
@ViewScoped
@Named
public class ManagerCriarPlayer implements Serializable {

    private final String STEAM_LOGIN_URL = "https://steamcommunity.com/openid/login";
    private final String RETURN_URL = "http://localhost:8080/PhbChampReloaded/criarPlayer.xhtml?redirectSteamLogin=true";

    @EJB
    PlayerServico playerServico;
    @EJB
    AnexoServico anexoServico;
    @EJB
    ConfiguracaoServico configuracaoServico;
    @EJB
    private JogoServico jogoServico;
    private Player player;
    private Player playerApagar;
    private Player playerPesquisar;
    private List<Player> players;
    private UploadedFile file;
    private String fileTemp;
    private StreamedContent imagem;
    private Configuracao configuracao;
    private List<Jogo> jogos;
    private List<Jogo> jogosSelecionados;
    private Jogo jogo;

    @PostConstruct
    public void init() {
        try {
            HttpServletRequest uri = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
            instanciar();

            String visualizarPlayerId = FacesUtil
                    .getRequestParameter("id");

            String redirectSteam = FacesUtil
                    .getRequestParameter("redirectSteamLogin");

            if (redirectSteam != null && !redirectSteam.isEmpty()) {
                processSteamReturn();
            }

            if (visualizarPlayerId != null && !visualizarPlayerId.isEmpty()) {
                this.player = this.playerServico.buscaPlayer(Long.parseLong(visualizarPlayerId));
                if (this.player.getId() != null && this.player.getAnexo() != null) {
                    this.fileTemp = this.player.getAnexo().getNome();
                }
            }

            if (uri.getRequestURI().contains("criarPlayer.xhtml") && this.player.getId() == null) {
                this.jogos = jogoServico.pesquisar();
            }
        } catch (Exception ex) {
            System.err.println(ex);
        }

    }

    public void instanciar() {
        this.player = new Player();
        this.playerPesquisar = new Player();
        this.playerApagar = new Player();
        this.players = null;
        this.configuracao = new Configuracao();
        this.jogo = new Jogo();
        this.jogos = new ArrayList<>();
        this.jogosSelecionados = new ArrayList<>();
    }

    public Player getPlayerPesquisar() {
        return playerPesquisar;
    }

    public void setPlayerPesquisar(Player playerPesquisar) {
        this.playerPesquisar = playerPesquisar;
    }

    public Player getP() {
        return player;
    }

    public Player getPlayerApagar() {
        return playerApagar;
    }

    public void setPlayerApagar(Player playerApagar) {
        this.playerApagar = playerApagar;
    }

    public void setP(Player player) {
        this.player = player;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public void setPlayers(List<Player> players) {
        this.players = players;
    }

    public UploadedFile getFile() {
        return file;
    }

    public void setFile(UploadedFile file) {
        this.file = file;
    }

    public StreamedContent getImagem() {
        return imagem;
    }

    public void setImagem(StreamedContent imagem) {
        this.imagem = imagem;
    }

    public Configuracao getConfiguracao() {
        return configuracao;
    }

    public void setConfiguracao(Configuracao configuracao) {
        this.configuracao = configuracao;
    }

    public void doUpload(FileUploadEvent event) {
        this.player.setAnexo(anexoServico.fileUploadTemp(event));
        this.fileTemp = this.player.getAnexo().getNome();
    }

    public List<Jogo> getJogos() {
        return jogos;
    }

    public void setJogos(List<Jogo> jogos) {
        this.jogos = jogos;
    }

    public Jogo getJogo() {
        return jogo;
    }

    public void setJogo(Jogo jogo) {
        this.jogo = jogo;
    }

    public List<Jogo> getJogosSelecionados() {
        return jogosSelecionados;
    }

    public void setJogosSelecionados(List<Jogo> jogosSelecionados) {
        this.jogosSelecionados = jogosSelecionados;
    }

    public void salvarPlayer() throws Exception {

        this.player.setJogos(this.jogosSelecionados);
        if (this.player.getAnexo() != null && this.player.getAnexo().getId() == null) {
            this.player.setAnexo(anexoServico.salvarAnexo(this.player.getAnexo()));
        }
        if (this.player.getId() == null) {
            this.player = playerServico.save(this.player, null, Url.SALVAR_PLAYER.getNome());
            Mensagem.successAndRedirect("Player salvo com sucesso", "visualizarPlayer.xhtml?id=" + this.player.getId());
        } else {
            this.player = playerServico.save(this.player, this.player.getId(), Url.ATUALIZAR_PLAYER.getNome());
            Mensagem.successAndRedirect("Player atualizado com sucesso", "visualizarPlayer.xhtml?id=" + this.player.getId());
        }

    }

    public void pesquisarPlayer() throws Exception {
        if (this.playerPesquisar.getNome() == null) {
            this.playerPesquisar.setNome("");
        }
        this.players = playerServico.pesquisar(this.playerPesquisar.getNome());
    }

    public void limpar() {
        instanciar();
    }

    public void removerPlayer() throws Exception {
        this.playerServico.delete(this.playerApagar, Url.APAGAR_PLAYER.getNome());
        Mensagem.successAndRedirect("pesquisarPlayer.xhtml");
        init();
    }

    public void adicionarJogo() {
        this.jogosSelecionados.add(this.jogo);
        this.jogo = new Jogo();

    }

    public String getFileTemp() {
        return fileTemp;
    }

    public void setFileTemp(String fileTemp) {
        this.fileTemp = fileTemp;
    }

    public List<Jogo> jogos() {
        return jogos;
    }

    public String buildSteamLoginUrl() {
        return buildSteamLoginUrlWithParams();
    }

    
    private String buildSteamLoginUrlWithParams() {
        String returnUrl = URLEncoder.encode(buildUrl(), StandardCharsets.UTF_8);
        return STEAM_LOGIN_URL + "?openid.mode=checkid_setup&openid.return_to=" + returnUrl
                + "&openid.ns=http://specs.openid.net/auth/2.0&openid.identity=http://specs.openid.net/auth/2.0/identifier_select"
                + "&openid.claimed_id=http://specs.openid.net/auth/2.0/identifier_select";
    }

    private void processSteamReturn() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) facesContext.getExternalContext().getRequest();

        Map<String, String[]> parameters = request.getParameterMap();
        String steamId = parameters.get("openid.claimed_id")[0];

        if (steamId != null && !steamId.isEmpty()) {
            try {
                // Decodificar a URL
                String decodedSteamId = URLDecoder.decode(steamId, StandardCharsets.UTF_8.name());

                // Extrair o SteamID64
                String steamId64 = decodedSteamId.substring(decodedSteamId.lastIndexOf("/") + 1);

                // Chamar o serviço de API da Steam
                PlayerSteamVo playerVo = new PlayerSteamVo();
                playerVo = playerServico.getPlayerInfo(steamId64, "F10A919CE16995E066B463C9005AF4D3");

                if (playerVo.getPersonaname() != null) {
                    this.player.setNick(playerVo.getPersonaname());
                }

                if (playerVo.getRealname() != null) {
                    this.player.setNome(playerVo.getRealname());
                }

                if (playerVo.getSteamid() != null) {
                    this.player.setSteamID(playerVo.getSteamid());
                }

                if (playerVo.getProfileurl() != null) {
                    this.player.setUrlSteam(playerVo.getProfileurl());
                }

            } catch (Exception e) {
                e.printStackTrace();
                Mensagem.error("Erro ao processar o retorno da Steam.");
            }
        } else {
            Mensagem.error("Erro ao processar o retorno da Steam.");
        }
    }

    public String buildUrl() {

        HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
        String scheme = request.getScheme();

        String serverName = request.getServerName();

        int serverPort = request.getServerPort();

        String baseUrl = scheme + "://" + serverName;

        if ((serverPort != 80 && "http".equals(scheme)) || (serverPort != 443 && "https".equals(scheme))) {
            baseUrl += ":" + serverPort;
        }

        String fixedPath = "/PhbChamp/criarPlayer.xhtml?redirectSteamLogin=true";

        return baseUrl + fixedPath;
    }
}
