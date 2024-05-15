/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.champ.Utilitario;

import jakarta.faces.context.FacesContext;

/**
 *
 * @author andre
 */
public class FacesUtil {

    public static FacesContext getFacesContext() {
        return FacesContext.getCurrentInstance();
    }

    public static String getRequestParameter(String name) {
        return getFacesContext()
                .getExternalContext()
                .getRequestParameterMap()
                .get(name);
    }

}
