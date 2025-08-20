/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package br.com.champ.Manager;

import br.com.champ.Modelo.Player;
import br.com.champ.Servico.PlayerServico;
import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import java.util.Map;

/**
 *
 * @author andre
 */
@Named("redirectManager")
@RequestScoped
public class ManagerRedirect {

    @EJB
    PlayerServico playerServico;

    public void processToken() {
        FacesContext fc = FacesContext.getCurrentInstance();
        Map<String, String> params = fc.getExternalContext().getRequestParameterMap();
        String token = params.get("token");
        //System.err.println(token);

        if (token != null && !token.isEmpty()) {
            try {

                DecodedJWT decodedJWT = JWT.decode(token);

                Long playerId = decodedJWT.getClaim("player_id").asLong();
                if (playerId != null) {
                    Player playerLogado = playerServico.buscaPlayer(playerId);
                    if (playerLogado != null && playerLogado.getSenha() == null) {
                        fc.getExternalContext().redirect("criarJogador.xhtml?id=" + playerLogado.getId() + "&cadastrar");
                    }
                } else {
                    fc.getExternalContext().redirect("index.xhtml");
                }

            } catch (Exception e) {
                e.printStackTrace();

            }
        }

    }

}
