/*
 * Value Object para dados da partida em tempo real
 */
package br.com.champ.vo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Dados da partida coletados em tempo real via RCON
 */
public class MatchData implements Serializable {
    
    private int scoreCT;
    private int scoreT;
    private int roundAtual;
    private int tempoRound; // segundos restantes
    private String estadoBomba; // "none", "planted", "defused", "exploded"
    private int tempoBomba; // segundos restantes se plantada
    private String mapa;
    private String fase; // "warmup", "live", "freezetime", "over"
    private List<PlayerInfo> playersCT;
    private List<PlayerInfo> playersT;
    private boolean conectado;
    private String ultimaAtualizacao;
    
    public MatchData() {
        playersCT = new ArrayList<>();
        playersT = new ArrayList<>();
        conectado = false;
        estadoBomba = "none";
    }
    
    public int getScoreCT() {
        return scoreCT;
    }
    
    public void setScoreCT(int scoreCT) {
        this.scoreCT = scoreCT;
    }
    
    public int getScoreT() {
        return scoreT;
    }
    
    public void setScoreT(int scoreT) {
        this.scoreT = scoreT;
    }
    
    public int getRoundAtual() {
        return roundAtual;
    }
    
    public void setRoundAtual(int roundAtual) {
        this.roundAtual = roundAtual;
    }
    
    public int getTempoRound() {
        return tempoRound;
    }
    
    public void setTempoRound(int tempoRound) {
        this.tempoRound = tempoRound;
    }
    
    public String getTempoRoundFormatado() {
        int minutos = tempoRound / 60;
        int segundos = tempoRound % 60;
        return String.format("%02d:%02d", minutos, segundos);
    }
    
    public String getEstadoBomba() {
        return estadoBomba;
    }
    
    public void setEstadoBomba(String estadoBomba) {
        this.estadoBomba = estadoBomba;
    }
    
    public int getTempoBomba() {
        return tempoBomba;
    }
    
    public void setTempoBomba(int tempoBomba) {
        this.tempoBomba = tempoBomba;
    }
    
    public String getTempoBombaFormatado() {
        if (tempoBomba <= 0) {
            return "00:00";
        }
        int minutos = tempoBomba / 60;
        int segundos = tempoBomba % 60;
        return String.format("%02d:%02d", minutos, segundos);
    }
    
    public String getMapa() {
        return mapa;
    }
    
    public void setMapa(String mapa) {
        this.mapa = mapa;
    }
    
    public String getFase() {
        return fase;
    }
    
    public void setFase(String fase) {
        this.fase = fase;
    }
    
    public List<PlayerInfo> getPlayersCT() {
        return playersCT;
    }
    
    public void setPlayersCT(List<PlayerInfo> playersCT) {
        this.playersCT = playersCT;
    }
    
    public List<PlayerInfo> getPlayersT() {
        return playersT;
    }
    
    public void setPlayersT(List<PlayerInfo> playersT) {
        this.playersT = playersT;
    }
    
    public boolean isConectado() {
        return conectado;
    }
    
    public void setConectado(boolean conectado) {
        this.conectado = conectado;
    }
    
    public String getUltimaAtualizacao() {
        return ultimaAtualizacao;
    }
    
    public void setUltimaAtualizacao(String ultimaAtualizacao) {
        this.ultimaAtualizacao = ultimaAtualizacao;
    }
}

