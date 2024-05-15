/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package br.com.champ.Manager;

import br.com.champ.Modelo.Partida;
import br.com.champ.Servico.PartidaServico;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


/**
 *
 * @author andre
 */
@ViewScoped
@Named
public class ManagerIndexPartida implements Serializable {
    
    @EJB
    private PartidaServico partidaServico;
    private List<Partida> partidas;
    
    @PostConstruct
    public void init() {
        this.partidas = partidaServico.pesquisarPartidasGeral();
        
    }
    
    public void instanciar() {
        this.partidas = new ArrayList<>();
    }
    
    public List<Partida> getPartidas() {
        return partidas;
    }
    
    public void setPartidas(List<Partida> partidas) {
        this.partidas = partidas;
    }
    
}
