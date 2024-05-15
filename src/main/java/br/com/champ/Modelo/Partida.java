/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.champ.Modelo;

import br.com.champ.Enums.Game;
import br.com.champ.Enums.Maps;
import br.com.champ.Generico.ModeloGenerico;
import java.io.Serializable;
import java.util.List;

/**
 *
 * @author andre
 */
public class Partida extends ModeloGenerico implements Serializable {

    private List<ItemPartida> itemPartida;
    private Jogo jogo;
    private Team timeVencedor;

    public Partida() {
    }

    public List<ItemPartida> getItemPartida() {
        return itemPartida;
    }

    public void setItemPartida(List<ItemPartida> itemPartida) {
        this.itemPartida = itemPartida;
    }

    public Jogo getJogo() {
        return jogo;
    }

    public void setJogo(Jogo jogo) {
        this.jogo = jogo;
    }

    public Team getTimeVencedor() {
        return timeVencedor;
    }

    public void setTimeVencedor(Team timeVencedor) {
        this.timeVencedor = timeVencedor;
    }

}
