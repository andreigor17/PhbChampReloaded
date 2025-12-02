/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Enum.java to edit this template
 */
package br.com.champ.Enums;

/**
 *
 * @author andre
 */
public enum Game {
    
    CS("CS"),
    PINGPONG("PING-PONG");

    private String nome;

    public String getNome(){
        return nome;
    }

    private Game(String nome){
        this.nome = nome;
    }
    
}
