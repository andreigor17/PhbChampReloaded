/*
 * Serviço para armazenar e gerenciar o estado do jogo recebido via GSI
 */
package br.com.champ.Servico;

import br.com.champ.vo.MatchData;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Serviço singleton para armazenar o estado atual do jogo recebido via GSI
 */
@Singleton
@Startup
public class GameStateService {
    
    private MatchData currentMatchData;
    private long lastUpdateTime;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private static final long TIMEOUT_MS = 10000; // 10 segundos sem atualização = desconectado
    
    public GameStateService() {
        currentMatchData = new MatchData();
        lastUpdateTime = System.currentTimeMillis();
    }
    
    /**
     * Atualiza o estado do jogo com novos dados do GSI
     */
    public void updateGameState(MatchData matchData) {
        lock.writeLock().lock();
        try {
            this.currentMatchData = matchData;
            this.lastUpdateTime = System.currentTimeMillis();
            
            // Atualiza timestamp de última atualização
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
            matchData.setUltimaAtualizacao(sdf.format(new Date()));
            
            // Marca como conectado se recebeu dados
            matchData.setConectado(true);
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    /**
     * Obtém o estado atual do jogo (cópia para evitar problemas de concorrência)
     */
    public MatchData getCurrentGameState() {
        lock.readLock().lock();
        try {
            MatchData copy = new MatchData();
            copy.setMapa(currentMatchData.getMapa());
            copy.setScoreCT(currentMatchData.getScoreCT());
            copy.setScoreT(currentMatchData.getScoreT());
            copy.setRoundAtual(currentMatchData.getRoundAtual());
            copy.setTempoRound(currentMatchData.getTempoRound());
            copy.setEstadoBomba(currentMatchData.getEstadoBomba());
            copy.setTempoBomba(currentMatchData.getTempoBomba());
            copy.setFase(currentMatchData.getFase());
            copy.setUltimaAtualizacao(currentMatchData.getUltimaAtualizacao());
            
            // Novos campos
            copy.setQuemPlantouBomba(currentMatchData.getQuemPlantouBomba());
            copy.setBombsite(currentMatchData.getBombsite());
            copy.setTimestampBombaPlantada(currentMatchData.getTimestampBombaPlantada());
            copy.setTimestampRoundInicio(currentMatchData.getTimestampRoundInicio());
            copy.setEventosRecentes(currentMatchData.getEventosRecentes() != null ? 
                new java.util.ArrayList<>(currentMatchData.getEventosRecentes()) : null);
            
            // Copia informações da última kill
            copy.setUltimaKillKiller(currentMatchData.getUltimaKillKiller());
            copy.setUltimaKillVictim(currentMatchData.getUltimaKillVictim());
            copy.setUltimaKillWeapon(currentMatchData.getUltimaKillWeapon());
            copy.setUltimaKillHeadshot(currentMatchData.isUltimaKillHeadshot());
            copy.setTimestampUltimaKill(currentMatchData.getTimestampUltimaKill());
            
            // Copia informações do MatchZy e partida do sistema
            copy.setMatchZyMatchId(currentMatchData.getMatchZyMatchId());
            copy.setPartidaId(currentMatchData.getPartidaId());
            copy.setPartidaNome(currentMatchData.getPartidaNome());
            copy.setPartidaTimeVencedor(currentMatchData.getPartidaTimeVencedor());
            copy.setPartidaTimePerdedor(currentMatchData.getPartidaTimePerdedor());
            if (currentMatchData.getPartidaTeams() != null) {
                copy.setPartidaTeams(new java.util.ArrayList<>(currentMatchData.getPartidaTeams()));
            }
            if (currentMatchData.getPartidaPlayers() != null) {
                copy.setPartidaPlayers(new java.util.ArrayList<>(currentMatchData.getPartidaPlayers()));
            }
            if (currentMatchData.getPlayersPorSteamId64() != null) {
                copy.setPlayersPorSteamId64(new java.util.HashMap<>(currentMatchData.getPlayersPorSteamId64()));
            }
            copy.setNomeTimeCT(currentMatchData.getNomeTimeCT());
            copy.setNomeTimeT(currentMatchData.getNomeTimeT());
            
            // Verifica se ainda está conectado (recebeu dados recentemente)
            long timeSinceLastUpdate = System.currentTimeMillis() - lastUpdateTime;
            copy.setConectado(timeSinceLastUpdate < TIMEOUT_MS && currentMatchData.isConectado());
            
            // Copia listas de players
            if (currentMatchData.getPlayersCT() != null) {
                copy.setPlayersCT(new java.util.ArrayList<>(currentMatchData.getPlayersCT()));
            }
            if (currentMatchData.getPlayersT() != null) {
                copy.setPlayersT(new java.util.ArrayList<>(currentMatchData.getPlayersT()));
            }
            
            // Atualiza timers antes de retornar
            copy.calcularTempoBombaRestante();
            copy.calcularTempoRoundRestante();
            
            return copy;
        } finally {
            lock.readLock().unlock();
        }
    }
    
    /**
     * Verifica se o serviço está recebendo dados recentes
     */
    public boolean isReceivingUpdates() {
        lock.readLock().lock();
        try {
            long timeSinceLastUpdate = System.currentTimeMillis() - lastUpdateTime;
            return timeSinceLastUpdate < TIMEOUT_MS;
        } finally {
            lock.readLock().unlock();
        }
    }
}

