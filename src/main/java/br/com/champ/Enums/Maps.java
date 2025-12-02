/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.champ.Enums;

/**
 *
 * @author andre
 */
public enum Maps {
    
     MIRAGE(1, "Mirage"),
    DUST2(2, "Dust2"),
    INFERNO(3,"Inferno"),
    OVERPASS(4,"Overpass"),
    NUKE(5,"Nuke"),
    VERTIGO(6,"Vertigo"),
    ANCIENT(7,"Ancient");
    
    private String nome;
    private Integer valor;

    public String getNome() {
        return nome;
    }

    public Integer getValor() {
        return valor;
    }
    
    

    private Maps(Integer valor, String nome) {
        this.nome = nome;
        this.valor = valor;
    }
    
}
