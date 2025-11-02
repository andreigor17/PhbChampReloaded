/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package br.com.champ.Servico;

import br.com.champ.Modelo.Player;
import br.com.champ.Modelo.Token;
import br.com.champ.Utilitario.APIPath;
import br.com.champ.Utilitario.RequisicaoUtils;
import br.com.champ.vo.LoginVo;
import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.gson.Gson;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

/**
 *
 * @author andre
 */
@Stateless
public class LoginServico {

    @Inject
    private HttpServletRequest request;
    @Inject
    private ExternalContext externalContext;

    @EJB
    private ConfiguracaoServico configuracaoServico;
    @EJB
    private TokenServico tokenServico;
    @EJB
    PlayerServico playerServico;

    public String pathToAPI() throws IOException {
        return APIPath.pathToAPI();

    }

    public String autenticar(LoginVo pessoa) {
        try {

            if (pessoa.getLogin() != null && !pessoa.getLogin().isBlank() && pessoa.getSenha() != null && !pessoa.getSenha().isBlank()) {
                Gson gson = new Gson();
                String json = gson.toJson(pessoa);

                try {

                    String token = RequisicaoUtils.requisicaoPost(pathToAPI() + "/api/login/auth", json);
                    if (token != null) {
                        FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("token", token);
                        return token;
                    }

                } catch (Exception e) {
                    return null;
                }

            }

            return null;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String autenticarSteam(LoginVo pessoa) {
        try {

            if (pessoa.getSteamId() != null && !pessoa.getSteamId().isBlank()) {
                Gson gson = new Gson();
                String json = gson.toJson(pessoa);

                try {

                    String token = RequisicaoUtils.requisicaoPost(pathToAPI() + "/api/login/steam", json);
                    if (token != null) {
                        System.err.println("tem token...");
                        FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("token", token);
                        return token;
                    }

                } catch (Exception e) {
                    return null;
                }

            }

            return null;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void deslogar() {
        try {

            FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("token", null);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void verificarLogin() {
        try {

            String paginaAtual = request.getRequestURL().toString();
            String token = (String) FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("token");
            boolean expirado = tokenServico.tokenExpirado(token);

            if ((expirado || token == null || token.isBlank()) && !paginaAtual.contains("login.xhtml")) {
                try {
                    externalContext.redirect("login.xhtml");
                } catch (Exception e) {
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean usuarioEstaLogado() {
        try {

            String token = (String) FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("token");
            boolean expirado = tokenServico.tokenExpirado(token);

            return !expirado;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public Player obterPlayerId() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        HttpSession sessao = (HttpSession) facesContext.getExternalContext().getSession(false);

        if (sessao != null) {

            String tokenJsonStr = (String) sessao.getAttribute("token");
            //System.err.println("token init " + tokenJsonStr);
            if (tokenJsonStr != null && !tokenJsonStr.isBlank() && !tokenJsonStr.isEmpty()) {
                try {

                    Gson gson = new Gson();
                    Token tokenObj = gson.fromJson(tokenJsonStr, Token.class);

                    if (tokenObj != null && tokenObj.getToken() != null) {

                        DecodedJWT decodedJWT = JWT.decode(tokenObj.getToken());

                        Long playerId = decodedJWT.getClaim("player_id").asLong();
                        if (playerId != null) {

                            Player playerLogado = playerServico.buscaPlayer(playerId);
                            if (playerLogado != null) {
                                return playerLogado;
                            }
                        } else {
                            return null;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();

                }
            }
        }
        return null;
    }
}
