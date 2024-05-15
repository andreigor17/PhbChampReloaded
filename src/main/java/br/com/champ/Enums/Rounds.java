/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Enum.java to edit this template
 */
package br.com.champ.Enums;

/**
 *
 * @author andreigor
 */
public enum Rounds {

    MD1(1, "MD1"),
    MD3(1, "MD3"),
    MD5(1, "MD5");
    private String nome;
    private Integer valor;

    public String getNome() {
        return nome;
    }

    public Integer getValor() {
        return valor;
    }

    private Rounds(Integer valor, String nome) {
        this.nome = nome;
        this.valor = valor;
    }

}
