/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.champ.Manager;

import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.Date;

/**
 *
 * @author andre
 */
@ViewScoped
@Named
public class ManagerHelper implements Serializable {

    public Date dataAtual() {
        return new Date();
    }

}
