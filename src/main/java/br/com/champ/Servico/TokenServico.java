/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package br.com.champ.Servico;

import br.com.champ.Modelo.Token;
import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.gson.Gson;
import jakarta.ejb.Stateless;
import jakarta.faces.context.FacesContext;
import java.util.Date;

/**
 *
 * @author andre
 */
@Stateless
public class TokenServico {

    public boolean tokenExpirado(String token) {
        try {

            if (token != null && !token.isBlank()) {
                Gson gson = new Gson();
                Token tokenJson = gson.fromJson(token, Token.class);
                DecodedJWT decodedJWT = JWT.decode(tokenJson.getToken());
                boolean expirado = decodedJWT.getExpiresAt().before(new Date());

                return expirado;

            }

            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return true;
        }
    }

    public String obterTokenDaSessao() {
        try {
            return (String) FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("token");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
