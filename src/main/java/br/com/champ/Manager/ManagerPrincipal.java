/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package br.com.champ.Manager;

import br.com.champ.Modelo.Player;
import br.com.champ.Servico.LoginServico;
import br.com.champ.Servico.PlayerServico;
import br.com.champ.Utilitario.FacesUtil;
import br.com.champ.Utilitario.Mensagem;
import br.com.champ.vo.LoginVo;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.Serializable;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author andrepc
 */
@Named
@ViewScoped
public class ManagerPrincipal implements Serializable {

    private final String STEAM_LOGIN_URL = "https://steamcommunity.com/openid/login";
    private final String RETURN_URL = "http://localhost:8080/PhbChampReloaded/criarPlayer.xhtml?redirectSteamLogin=true";

    @Inject
    private HttpServletRequest request;
    @Inject
    private ExternalContext externalContext;
    @EJB
    LoginServico loginServico;
    @EJB
    PlayerServico playerServico;
    private Player player;
    private String token;
    private String tokenReset;
    private LoginVo usuario;
    private Player playerLogado;

    @PostConstruct
    public void init() {
        try {

            instanciar();
            HttpServletRequest uri = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
            String redirectSteam = FacesUtil
                    .getRequestParameter("redirectSteamLogin");

            

            String deslogar = FacesUtil
                    .getRequestParameter("lougout");

            if (deslogar != null) {
                loginServico.deslogar();
                Mensagem.successAndRedirect("Logout realizado com sucesso!", "index.xhtml");

            }
            this.playerLogado = null;
            Long playerId = loginServico.obterPlayerId();
            if (playerId != null) {
                this.playerLogado = playerServico.buscaPlayer(playerId);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void instanciar() {
        try {

            this.player = new Player();
            this.usuario = new LoginVo();
            this.playerLogado = new Player();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean usuarioEstaLogado() {
        try {

            return loginServico.usuarioEstaLogado();

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void login() {
        try {

            if (loginServico.autenticar(this.usuario) != null) {
                externalContext.redirect("index.xhtml");
            } else {
                Mensagem.error("Email ou senha inválidos! Verifique e tente novamente.");
            }

        } catch (Exception e) {
            Mensagem.error("Erro ao efetuar login.");
            e.printStackTrace();
        }
    }

    public void sair() {
        try {

            loginServico.deslogar();

            Mensagem.successAndRedirect("Você saiu do sistema com sucesso!", "index.xhtml");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getTokenReset() {
        return tokenReset;
    }

    public void setTokenReset(String tokenReset) {
        this.tokenReset = tokenReset;
    }

    public LoginVo getUsuario() {
        return usuario;
    }

    public void setUsuario(LoginVo usuario) {
        this.usuario = usuario;
    }

    public Player getPlayerLogado() {
        return playerLogado;
    }

    public void setPlayerLogado(Player playerLogado) {
        this.playerLogado = playerLogado;
    }

    public String buildSteamLoginUrl() {
        return buildSteamLoginUrlWithParams();
    }

    public void redirecionarSteam() {

        try {
            String url = buildSteamLoginUrlWithParams();
            FacesContext.getCurrentInstance().getExternalContext().redirect(url);
        } catch (IOException ex) {
            Logger.getLogger(ManagerPrincipal.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private String buildSteamLoginUrlWithParams() {
        String returnUrl = URLEncoder.encode(buildUrl(), StandardCharsets.UTF_8);
        return STEAM_LOGIN_URL + "?openid.mode=checkid_setup&openid.return_to=" + returnUrl
                + "&openid.ns=http://specs.openid.net/auth/2.0&openid.identity=http://specs.openid.net/auth/2.0/identifier_select"
                + "&openid.claimed_id=http://specs.openid.net/auth/2.0/identifier_select";
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

        String fixedPath = "/PhbChampReloaded/criarPlayer.xhtml?redirectSteamLogin=true";

        return baseUrl + fixedPath;
    }

    
}
