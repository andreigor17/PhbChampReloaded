/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package br.com.champ.Servico;

import br.com.champ.Modelo.Player;
import br.com.champ.Utilitario.APIPath;
import br.com.champ.Utilitario.RequisicaoUtils;
import com.google.gson.Gson;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
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

    public String pathToAPI() throws IOException {
        return APIPath.pathToAPI();

    }

    public String autenticar(Player pessoa) {
        try {

            if (pessoa.getEmail() != null && !pessoa.getEmail().isBlank() && pessoa.getSenha() != null && !pessoa.getSenha().isBlank()) {
                Gson gson = new Gson();
                String json = gson.toJson(pessoa);

                try {

                    String token = RequisicaoUtils.requisicaoPost(pathToAPI() + "/api/login", json);
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
}
