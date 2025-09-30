/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package br.com.champ.Manager;

import br.com.champ.Modelo.LocalPartida;
import br.com.champ.Modelo.Team;
import br.com.champ.Servico.LocalServico;
import br.com.champ.Servico.TeamServico;
import jakarta.ejb.EJB;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.List;

/**
 *
 * @author andre
 */
@ViewScoped
@Named
public class ManagerAutoCompletes implements Serializable {

    @EJB
    private TeamServico teamServico;
    @EJB
    private LocalServico localServico;

    public List<Team> autoCompletarTime() throws Exception {
        return teamServico.autoCompleteTime();
    }
    
    public List<LocalPartida> autoCompletarLocalPartida() throws Exception {
        return localServico.autoCompleteLocal();
    }

}
