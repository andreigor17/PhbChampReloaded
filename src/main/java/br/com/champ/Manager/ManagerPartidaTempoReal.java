/*
 * Manager para coletar dados da partida em tempo real via GSI (Game State Integration)
 */
package br.com.champ.Manager;

import br.com.champ.Servico.GameStateService;
import br.com.champ.vo.MatchData;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;

/**
 * Manager para monitorar partida em tempo real usando dados do GSI
 */
@Named
@ViewScoped
public class ManagerPartidaTempoReal extends ManagerBase {

    @EJB
    private GameStateService gameStateService;

    private MatchData matchData;
    private boolean monitorando;
    
    @PostConstruct
    public void init() {
        matchData = new MatchData();
        monitorando = false;
        atualizarDados();
    }
    
    /**
     * Atualiza os dados da partida via GSI
     * Os dados são recebidos automaticamente pelo GSIResource quando o jogo envia atualizações
     */
    public void atualizarDados() {
        try {
            // Obtém o estado atual do jogo do serviço GSI
            MatchData gsiData = gameStateService.getCurrentGameState();
            
            if (gsiData != null) {
                // Atualiza o matchData local com os dados do GSI
                matchData.setMapa(gsiData.getMapa());
                matchData.setScoreCT(gsiData.getScoreCT());
                matchData.setScoreT(gsiData.getScoreT());
                matchData.setRoundAtual(gsiData.getRoundAtual());
                matchData.setTempoRound(gsiData.getTempoRound());
                matchData.setEstadoBomba(gsiData.getEstadoBomba());
                matchData.setTempoBomba(gsiData.getTempoBomba());
                matchData.setFase(gsiData.getFase());
                matchData.setConectado(gsiData.isConectado());
                matchData.setUltimaAtualizacao(gsiData.getUltimaAtualizacao());
                matchData.setPlayersCT(gsiData.getPlayersCT());
                matchData.setPlayersT(gsiData.getPlayersT());
            } else {
                matchData.setConectado(false);
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            matchData.setConectado(false);
        }
    }
    
    /**
     * Inicia monitoramento automático
     */
    public void iniciarMonitoramento() {
        monitorando = true;
    }
    
    /**
     * Para monitoramento automático
     */
    public void pararMonitoramento() {
        monitorando = false;
    }
    
    /**
     * Testa a conexão GSI (verifica se está recebendo dados)
     */
    public void testarConexao() {
        atualizarDados();
    }
    
    // Getters e Setters
    public MatchData getMatchData() {
        return matchData;
    }
    
    public void setMatchData(MatchData matchData) {
        this.matchData = matchData;
    }
    
    public boolean isMonitorando() {
        return monitorando;
    }
    
    public void setMonitorando(boolean monitorando) {
        this.monitorando = monitorando;
    }
}
