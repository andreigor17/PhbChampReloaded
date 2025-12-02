package br.com.champ.Modelo;

import br.com.champ.Generico.ModeloGenerico;
import java.io.Serializable;
import java.util.List;

/**
 *
 * @author andre
 */
public class Tabela extends ModeloGenerico implements Serializable{
    
    private String team;
    private int roundsGanhos;
    private int roundsPerdidos;
    private int partidas;
    private int kills;
    private int deaths;
    private int assists;
    private int pontos;

    public Tabela(String team, int roundsGanhos, int roundsPerdidos, int partidas, int kills, int deaths, int assists, int pontos) {
        this.team = team;
        this.roundsGanhos = roundsGanhos;
        this.roundsPerdidos = roundsPerdidos;
        this.partidas = partidas;
        this.kills = kills;
        this.deaths = deaths;
        this.assists = assists;
        this.pontos = pontos;
    }

    public String getTeam() {
        return team;
    }

    public void setTeam(String team) {
        this.team = team;
    }

    public int getRoundsGanhos() {
        return roundsGanhos;
    }

    public void setRoundsGanhos(int roundsGanhos) {
        this.roundsGanhos = roundsGanhos;
    }

    public int getRoundsPerdidos() {
        return roundsPerdidos;
    }

    public void setRoundsPerdidos(int roundsPerdidos) {
        this.roundsPerdidos = roundsPerdidos;
    }

    public int getPartidas() {
        return partidas;
    }

    public void setPartidas(int partidas) {
        this.partidas = partidas;
    }

    public int getKills() {
        return kills;
    }

    public void setKills(int kills) {
        this.kills = kills;
    }

    public int getDeaths() {
        return deaths;
    }

    public void setDeaths(int deaths) {
        this.deaths = deaths;
    }

    public int getAssists() {
        return assists;
    }

    public void setAssists(int assists) {
        this.assists = assists;
    }

    public int getPontos() {
        return pontos;
    }

    public void setPontos(int pontos) {
        this.pontos = pontos;
    }
    
    
    
}
