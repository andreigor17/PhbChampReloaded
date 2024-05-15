/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.champ.Modelo;

import br.com.champ.Generico.ModeloGenerico;
import java.io.Serializable;

/**
 *
 * @author andre
 */
public class ItemPartida extends ModeloGenerico implements Serializable {

    private Integer scoreT1;
    private Integer scoreT2;
    private Mapas mapas;
    private Long partida;
    private Team team1;
    private Team team2;
    private Player player1;
    private Player player2;
    private Long camp;
    private Estatisticas estatisticas;
    private Team timeVencedor;
    private boolean jogado;

    public Integer getScoreT1() {
        return scoreT1;
    }

    public void setScoreT1(Integer scoreT1) {
        this.scoreT1 = scoreT1;
    }

    public Integer getScoreT2() {
        return scoreT2;
    }

    public void setScoreT2(Integer scoreT2) {
        this.scoreT2 = scoreT2;
    }

    public Mapas getMapas() {
        return mapas;
    }

    public void setMapas(Mapas mapas) {
        this.mapas = mapas;
    }

    public Team getTeam1() {
        return team1;
    }

    public void setTeam1(Team team1) {
        this.team1 = team1;
    }

    public Team getTeam2() {
        return team2;
    }

    public void setTeam2(Team team2) {
        this.team2 = team2;
    }

    public Estatisticas getEstatisticas() {
        return estatisticas;
    }

    public void setEstatisticas(Estatisticas estatisticas) {
        this.estatisticas = estatisticas;
    }

    public Long getPartida() {
        return partida;
    }

    public void setPartida(Long partida) {
        this.partida = partida;
    }

    public Long getCamp() {
        return camp;
    }

    public void setCamp(Long camp) {
        this.camp = camp;
    }

    public Player getPlayer1() {
        return player1;
    }

    public void setPlayer1(Player player1) {
        this.player1 = player1;
    }

    public Player getPlayer2() {
        return player2;
    }

    public void setPlayer2(Player player2) {
        this.player2 = player2;
    }

    public Team getTimeVencedor() {
        return timeVencedor;
    }

    public void setTimeVencedor(Team timeVencedor) {
        this.timeVencedor = timeVencedor;
    }

    public boolean isJogado() {
        return jogado;
    }

    public void setJogado(boolean jogado) {
        this.jogado = jogado;
    }

}
