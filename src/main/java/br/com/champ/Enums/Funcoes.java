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
public enum Funcoes {
    
    RIFLER(1, "Rifler"),
    AWPER(2, "Awper"),
    LURKER(3, "Lurker"),
    IGL(4, "Igl"),
    COACH(5, "Coach"),
    ENTRY(5, "Entry");
    
    private String nome;
    private Integer valor;

    public String getNome() {
        return nome;
    }

    public Integer getValor() {
        return valor;
    }
    
    

    private Funcoes(Integer valor, String nome) {
        this.nome = nome;
        this.valor = valor;
    }

    @Override
    public String toString() {
        return super.toString(); //To change body of generated methods, choose Tools | Templates.
    }
    
    
    
}
