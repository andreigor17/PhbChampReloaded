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
    private String quemPlantouBomba; // Nome do jogador que plantou
    private String bombsite; // "A" ou "B"
    private long timestampBombaPlantada; // Timestamp quando foi plantada (para calcular countdown)
    private long timestampRoundInicio; // Timestamp quando round começou (para calcular countdown de 2min)
    private List<String> eventosRecentes; // Últimos eventos para exibir na tela
    private int tempoBombaRestante; // Calculado baseado no timestamp
    
    // Informações da última kill registrada nos logs
    private String ultimaKillKiller;
    private String ultimaKillVictim;
    private String ultimaKillWeapon;
    private boolean ultimaKillHeadshot;
    private long timestampUltimaKill;
    
    public MatchData() {
        playersCT = new ArrayList<>();
        playersT = new ArrayList<>();
        conectado = false;
        estadoBomba = "none";
        eventosRecentes = new ArrayList<>();
        tempoBombaRestante = 0;
        ultimaKillKiller = null;
        ultimaKillVictim = null;
        ultimaKillWeapon = null;
        ultimaKillHeadshot = false;
        timestampUltimaKill = 0L;
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
    
    public String getQuemPlantouBomba() {
        return quemPlantouBomba;
    }
    
    public void setQuemPlantouBomba(String quemPlantouBomba) {
        this.quemPlantouBomba = quemPlantouBomba;
    }
    
    public String getBombsite() {
        return bombsite;
    }
    
    public void setBombsite(String bombsite) {
        this.bombsite = bombsite;
    }
    
    public long getTimestampBombaPlantada() {
        return timestampBombaPlantada;
    }
    
    public void setTimestampBombaPlantada(long timestampBombaPlantada) {
        this.timestampBombaPlantada = timestampBombaPlantada;
    }
    
    public long getTimestampRoundInicio() {
        return timestampRoundInicio;
    }
    
    public void setTimestampRoundInicio(long timestampRoundInicio) {
        this.timestampRoundInicio = timestampRoundInicio;
    }
    
    public List<String> getEventosRecentes() {
        return eventosRecentes;
    }
    
    public void setEventosRecentes(List<String> eventosRecentes) {
        this.eventosRecentes = eventosRecentes;
    }
    
    public int getTempoBombaRestante() {
        return tempoBombaRestante;
    }
    
    public void setTempoBombaRestante(int tempoBombaRestante) {
        this.tempoBombaRestante = tempoBombaRestante;
    }
    
    public String getUltimaKillKiller() {
        return ultimaKillKiller;
    }
    
    public void setUltimaKillKiller(String ultimaKillKiller) {
        this.ultimaKillKiller = ultimaKillKiller;
    }
    
    public String getUltimaKillVictim() {
        return ultimaKillVictim;
    }
    
    public void setUltimaKillVictim(String ultimaKillVictim) {
        this.ultimaKillVictim = ultimaKillVictim;
    }
    
    public String getUltimaKillWeapon() {
        return ultimaKillWeapon;
    }
    
    public void setUltimaKillWeapon(String ultimaKillWeapon) {
        this.ultimaKillWeapon = ultimaKillWeapon;
    }
    
    public boolean isUltimaKillHeadshot() {
        return ultimaKillHeadshot;
    }
    
    public void setUltimaKillHeadshot(boolean ultimaKillHeadshot) {
        this.ultimaKillHeadshot = ultimaKillHeadshot;
    }
    
    public long getTimestampUltimaKill() {
        return timestampUltimaKill;
    }
    
    public void setTimestampUltimaKill(long timestampUltimaKill) {
        this.timestampUltimaKill = timestampUltimaKill;
    }
    
    /**
     * Calcula o tempo restante da bomba baseado no timestamp
     */
    public void calcularTempoBombaRestante() {
        if (timestampBombaPlantada > 0 && "planted".equals(estadoBomba)) {
            long agora = System.currentTimeMillis();
            long decorrido = (agora - timestampBombaPlantada) / 1000; // segundos
            tempoBombaRestante = Math.max(0, 40 - (int) decorrido);
            
            if (tempoBombaRestante <= 0) {
                estadoBomba = "exploded";
                tempoBombaRestante = 0;
            }
        } else {
            tempoBombaRestante = 0;
        }
    }
    
    /**
     * Calcula o tempo restante do round baseado no timestamp
     */
    public void calcularTempoRoundRestante() {
        if (timestampRoundInicio > 0) {
            long agora = System.currentTimeMillis();
            long decorrido = (agora - timestampRoundInicio) / 1000; // segundos
            
            if ("freezetime".equals(fase)) {
                // Freeze time = 20 segundos
                tempoRound = Math.max(0, 20 - (int) decorrido);
                // Se acabou o freeze time, muda para live
                if (tempoRound <= 0) {
                    fase = "live";
                    timestampRoundInicio = System.currentTimeMillis(); // Reinicia contagem para round
                    tempoRound = 120; // 2 minutos de round
                }
            } else if ("live".equals(fase)) {
                // Round em andamento = 2 minutos
                tempoRound = Math.max(0, 120 - (int) decorrido);
                
                if (tempoRound <= 0) {
                    tempoRound = 0;
                }
            }
        }
    }
    
    /**
     * Retorna o tempo da bomba formatado
     */
    public String getTempoBombaRestanteFormatado() {
        if (tempoBombaRestante <= 0) {
            return "00:00";
        }
        int segundos = tempoBombaRestante;
        return String.format("00:%02d", segundos);
    }
}

