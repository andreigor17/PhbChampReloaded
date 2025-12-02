/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package br.com.champ.vo;

import br.com.champ.Modelo.Partida;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author andre
 */
public class BracketVo {

    private List<Partida> upperBracket = new ArrayList<>();
    private List<Partida> lowerBracket = new ArrayList<>();

    public List<Partida> getUpperBracket() {
        return upperBracket;
    }

    public void setUpperBracket(List<Partida> upperBracket) {
        this.upperBracket = upperBracket;
    }

    public List<Partida> getLowerBracket() {
        return lowerBracket;
    }

    public void setLowerBracket(List<Partida> lowerBracket) {
        this.lowerBracket = lowerBracket;
    }

}
