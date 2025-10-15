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
public enum TipoCampeonato {
    PONTOS_CORRIDOS(1, "PONTOS CORRIDOS"),
    MATA_MATA(3, "MATA-MATA"),
    SUICO(4, "SUÍÇO");

    private String nome;
    private Integer valor;

    public String getNome() {
        return nome;
    }

    public Integer getValor() {
        return valor;
    }

    private TipoCampeonato(Integer valor, String nome) {
        this.nome = nome;
        this.valor = valor;
    }
}
