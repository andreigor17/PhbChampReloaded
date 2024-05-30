/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package br.com.champ.Modelo;

import br.com.champ.Generico.ModeloGenerico;
import java.util.List;

/**
 *
 * @author andre
 */
public class Fase extends ModeloGenerico {

    private List<Partida> partidas;
    private String nome;
    private Long idCamp;

    public List<Partida> getPartidas() {
        return partidas;
    }

    public void setPartidas(List<Partida> partidas) {
        this.partidas = partidas;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public Long getIdCamp() {
        return idCamp;
    }

    public void setIdCamp(Long idCamp) {
        this.idCamp = idCamp;
    }

}
