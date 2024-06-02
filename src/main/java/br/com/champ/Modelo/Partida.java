/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.champ.Modelo;

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
    private int contador;
    private int contadorEsperado;
    private boolean finalizada;
    private Long faseid;

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

    public int getContador() {
        return contador;
    }

    public void setContador(int contador) {
        this.contador = contador;
    }

    public int getContadorEsperado() {
        return contadorEsperado;
    }

    public void setContadorEsperado(int contadorEsperado) {
        this.contadorEsperado = contadorEsperado;
    }

    public boolean isFinalizada() {
        return finalizada;
    }

    public void setFinalizada(boolean finalizada) {
        this.finalizada = finalizada;
    }

    public Long getFaseid() {
        return faseid;
    }

    public void setFaseid(Long faseid) {
        this.faseid = faseid;
    }

}
