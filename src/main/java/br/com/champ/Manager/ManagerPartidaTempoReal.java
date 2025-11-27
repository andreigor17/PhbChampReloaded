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
 * Usa @ViewScoped mas com tratamento especial para evitar ViewExpiredException
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
        try {
            matchData = new MatchData();
            monitorando = true; // Sempre monitorando desde o início
            atualizarDados();
        } catch (Exception e) {
            System.err.println("Erro no init do ManagerPartidaTempoReal: " + e.getMessage());
            if (matchData == null) {
                matchData = new MatchData();
            }
        }
    }
    
    /**
     * Verifica se a view JSF está válida e pode ser usada
     */
    private boolean isViewValid() {
        try {
            jakarta.faces.context.FacesContext facesContext = jakarta.faces.context.FacesContext.getCurrentInstance();
            if (facesContext == null) {
                return false;
            }
            
            jakarta.faces.component.UIViewRoot viewRoot = facesContext.getViewRoot();
            if (viewRoot == null) {
                return false;
            }
            
            // Verifica se a view ainda está ativa
            String viewId = viewRoot.getViewId();
            if (viewId == null || !viewId.contains("partidaTempoReal.xhtml")) {
                return false;
            }
            
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Atualiza os dados da partida via GSI
     * Os dados são recebidos automaticamente pelo GSIResource quando o jogo envia atualizações
     * Método protegido contra ViewExpiredException
     */
    public void atualizarDados() {
        // Verifica se a view está válida ANTES de qualquer processamento
        if (!isViewValid()) {
            // View não está válida - retorna silenciosamente
            // O JavaScript detectará e recarregará a página
            return;
        }
        
        try {
            // Inicializa matchData se necessário
            if (matchData == null) {
                matchData = new MatchData();
            }
            
            // Obtém o estado atual do jogo do serviço GSI (não depende da view)
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
                
                // Novos campos adicionados
                matchData.setQuemPlantouBomba(gsiData.getQuemPlantouBomba());
                matchData.setBombsite(gsiData.getBombsite());
                matchData.setTimestampBombaPlantada(gsiData.getTimestampBombaPlantada());
                matchData.setTimestampRoundInicio(gsiData.getTimestampRoundInicio());
                matchData.setEventosRecentes(gsiData.getEventosRecentes());
                
                // Novos campos da partida do sistema
                matchData.setMatchZyMatchId(gsiData.getMatchZyMatchId());
                matchData.setPartidaId(gsiData.getPartidaId());
                matchData.setPartidaNome(gsiData.getPartidaNome());
                matchData.setPartidaTimeVencedor(gsiData.getPartidaTimeVencedor());
                matchData.setPartidaTimePerdedor(gsiData.getPartidaTimePerdedor());
                matchData.setPartidaTeams(gsiData.getPartidaTeams());
                matchData.setPartidaPlayers(gsiData.getPartidaPlayers());
                
                // Atualiza timers baseados nos timestamps
                matchData.calcularTempoBombaRestante();
                matchData.calcularTempoRoundRestante();
            } else {
                if (matchData == null) {
                    matchData = new MatchData();
                }
                matchData.setConectado(false);
            }
            
        } catch (jakarta.faces.application.ViewExpiredException e) {
            // View expirada - não faz nada, o JavaScript tratará
            System.out.println("ViewExpiredException capturada em atualizarDados");
            return;
        } catch (IllegalStateException e) {
            // Contexto JSF não disponível
            if (e.getMessage() != null && (e.getMessage().contains("ViewRoot") || e.getMessage().contains("FacesContext"))) {
                System.out.println("View não disponível - ignorando atualização");
                return;
            }
            throw e;
        } catch (Exception e) {
            System.err.println("Erro ao atualizar dados: " + e.getMessage());
            // Não inicializa matchData aqui para evitar problemas com view expirada
            if (matchData != null && isViewValid()) {
                matchData.setConectado(false);
            }
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
