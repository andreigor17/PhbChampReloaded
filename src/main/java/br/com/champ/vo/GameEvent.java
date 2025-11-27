/*
 * Classe para representar eventos do jogo (kills, bomb plant, etc)
 */
package br.com.champ.vo;

import java.io.Serializable;
import java.util.Date;

/**
 * Representa um evento ocorrido no jogo
 */
public class GameEvent implements Serializable {
    
    private String tipo; // "kill", "bomb_planted", "bomb_defused", "round_start", "round_end", "got_bomb"
    private String jogador1; // Quem fez a ação (killer, planter, etc)
    private String jogador2; // Vítima (se aplicável)
    private String arma; // Arma usada
    private boolean headshot; // Se foi headshot
    private String bombsite; // Bombsite A ou B
    private Date timestamp;
    
    public GameEvent() {
        this.timestamp = new Date();
    }
    
    public GameEvent(String tipo) {
        this();
        this.tipo = tipo;
    }
    
    // Getters e Setters
    public String getTipo() {
        return tipo;
    }
    
    public void setTipo(String tipo) {
        this.tipo = tipo;
    }
    
    public String getJogador1() {
        return jogador1;
    }
    
    public void setJogador1(String jogador1) {
        this.jogador1 = jogador1;
    }
    
    public String getJogador2() {
        return jogador2;
    }
    
    public void setJogador2(String jogador2) {
        this.jogador2 = jogador2;
    }
    
    public String getArma() {
        return arma;
    }
    
    public void setArma(String arma) {
        this.arma = arma;
    }
    
    public boolean isHeadshot() {
        return headshot;
    }
    
    public void setHeadshot(boolean headshot) {
        this.headshot = headshot;
    }
    
    public String getBombsite() {
        return bombsite;
    }
    
    public void setBombsite(String bombsite) {
        this.bombsite = bombsite;
    }
    
    public Date getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
    
    public String getDescricao() {
        switch (tipo) {
            case "kill":
                return jogador1 + " matou " + jogador2 + (arma != null ? " com " + arma : "") + (headshot ? " (HEADSHOT)" : "");
            case "bomb_planted":
                return jogador1 + " plantou a bomba em bombsite " + bombsite;
            case "bomb_defused":
                return jogador1 + " desarmou a bomba";
            case "bomb_exploded":
                return "Bomba explodiu!";
            case "got_bomb":
                return jogador1 + " pegou a bomba";
            case "round_start":
                return "Round iniciado - Freeze time";
            case "round_end":
                return "Round finalizado";
            default:
                return tipo;
        }
    }
}



