/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.champ.Manager;

import br.com.champ.Servico.LoginServico;
import br.com.champ.vo.UsuarioVo;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpServletRequest;
import java.io.Serializable;

/**
 *
 * @author andre
 */
@Named
@ViewScoped
public class ManagerLogin implements Serializable {

    @Inject
    private HttpServletRequest request;
    @Inject
    private ExternalContext externalContext;
    private UsuarioVo usuario;

    @EJB
    LoginServico loginServico;

    @PostConstruct
    public void init() {
        try {

            loginServico.verificarLogin();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
