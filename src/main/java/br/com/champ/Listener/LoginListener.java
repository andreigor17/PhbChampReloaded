/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package br.com.champ.Listener;

import jakarta.faces.context.FacesContext;
import jakarta.faces.event.PhaseEvent;
import jakarta.faces.event.PhaseId;
import jakarta.faces.event.PhaseListener;
import jakarta.servlet.http.HttpSession;

/**
 *
 * @author andre
 */
public class LoginListener implements PhaseListener {

    @Override
    public void afterPhase(PhaseEvent event) {

        try {
//            FacesContext context = event.getFacesContext();
//            ManagerPrincipal managerPrincipal = context.getApplication().evaluateExpressionGet(context, "#{managerPrincipal}", ManagerPrincipal.class);
//            FacesContext facesContext = event.getFacesContext();
//            HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(true);
//            String token = loginServico.verificarLogin(managerPrincipal.getEmail(), managerPrincipal.getSenha());
//            String currentPage = facesContext.getViewRoot().getViewId();
//
//            System.out.println(token);
//
//            if (token != null && !token.isBlank() && currentPage.contains("login")) {
//                session.setAttribute("token", token);
//                facesContext.getExternalContext().redirect("index.xhtml");
//            }
//
//            if (session.getAttribute("token") != null && currentPage.contains("login")) {
//                facesContext.getExternalContext().redirect("index.xhtml");
//            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void beforePhase(PhaseEvent event) {
        FacesContext facesContext = event.getFacesContext();
        HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(true);
        session.getAttribute("token");
    }

    @Override
    public PhaseId getPhaseId() {
        return PhaseId.ANY_PHASE;
    }

}
