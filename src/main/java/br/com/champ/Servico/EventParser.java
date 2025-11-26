/*
 * Parser para extrair eventos dos logs do CS2
 */
package br.com.champ.Servico;

import br.com.champ.vo.MatchData;
import jakarta.ejb.Stateless;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parser para extrair eventos importantes dos logs do CS2
 */
@Stateless
public class EventParser {
    
    private static final int MAX_EVENTOS = 20; // Máximo de eventos recentes
    
    /**
     * Extrai eventos do conteúdo recebido e atualiza o MatchData
     */
    public void parseEvents(String content, MatchData matchData) {
        if (content == null || content.trim().isEmpty()) {
            return;
        }
        
        List<String> eventos = matchData.getEventosRecentes();
        if (eventos == null) {
            eventos = new ArrayList<>();
            matchData.setEventosRecentes(eventos);
        }
        
        // Parse de eventos específicos
        parseRoundStart(content, matchData);
        parseBombPlanted(content, matchData, eventos);
        parseBombGot(content, matchData, eventos);
        parseKill(content, matchData, eventos);
        parseRoundEnd(content, matchData, eventos);
        
        // Mantém apenas os últimos N eventos
        while (eventos.size() > MAX_EVENTOS) {
            eventos.remove(0);
        }
    }
    
    /**
     * Parse: "Starting Freeze period"
     */
    private void parseRoundStart(String content, MatchData matchData) {
        if (content.contains("Starting Freeze period")) {
            matchData.setFase("freezetime");
            matchData.setTimestampRoundInicio(System.currentTimeMillis());
            matchData.setTempoRound(20); // Freeze time = 20 segundos
            matchData.setEstadoBomba("none");
            matchData.setTimestampBombaPlantada(0);
            
            // Adiciona evento
            List<String> eventos = matchData.getEventosRecentes();
            if (eventos == null) {
                eventos = new ArrayList<>();
                matchData.setEventosRecentes(eventos);
            }
            eventos.add("Round iniciado - Freeze time (20s)");
        }
        
        // Detecta quando round realmente começa (após freeze time)
        // Pode ser "Round_Start" ou quando freeze time acaba (detectado pelo timer)
        if (content.contains("Round_Start") || content.contains("Round started")) {
            matchData.setFase("live");
            // Reinicia timestamp para contar 2 minutos
            matchData.setTimestampRoundInicio(System.currentTimeMillis());
            matchData.setTempoRound(120); // 2 minutos de round
        }
    }
    
    /**
     * Parse: "Planted_The_Bomb" at bombsite A/B
     * Exemplo: "Dede<2><[U:1:195353036]><TERRORIST>" triggered "Planted_The_Bomb" at bombsite A
     */
    private void parseBombPlanted(String content, MatchData matchData, List<String> eventos) {
        Pattern pattern = Pattern.compile("\"([^<\"]+)<[^>]*><[^>]*><[^>]*>\"\\s+triggered\\s+\"Planted_The_Bomb\"\\s+at\\s+bombsite\\s+([AB])", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(content);
        
        if (matcher.find()) {
            String player = matcher.group(1).trim();
            String site = matcher.group(2).toUpperCase();
            
            matchData.setEstadoBomba("planted");
            matchData.setQuemPlantouBomba(player);
            matchData.setBombsite(site);
            matchData.setTimestampBombaPlantada(System.currentTimeMillis());
            matchData.setTempoBombaRestante(40);
            
            String evento = "💣 " + player + " plantou a bomba no bombsite " + site;
            if (eventos == null) {
                eventos = new ArrayList<>();
                matchData.setEventosRecentes(eventos);
            }
            eventos.add(evento);
        }
    }
    
    /**
     * Parse: "Got_The_Bomb"
     * Exemplo: "Dede<2><[U:1:195353036]><TERRORIST>" triggered "Got_The_Bomb"
     * NOTA: Este evento é ignorado pois acontece muito frequentemente e não é relevante para exibição
     */
    private void parseBombGot(String content, MatchData matchData, List<String> eventos) {
        // Ignorado - evento muito frequente e não relevante para exibição
        // Se quiser exibir, descomente o código abaixo:
        /*
        Pattern pattern = Pattern.compile("\"([^<\"]+)<[^>]*><[^>]*><[^>]*>\"\\s+triggered\\s+\"Got_The_Bomb\"");
        Matcher matcher = pattern.matcher(content);
        
        if (matcher.find()) {
            String player = matcher.group(1).trim();
            String evento = "📦 " + player + " pegou a bomba";
            if (eventos == null) {
                eventos = new ArrayList<>();
                matchData.setEventosRecentes(eventos);
            }
            eventos.add(evento);
        }
        */
    }
    
    /**
     * Parse: killed
     * Exemplo: "Dede<2><[U:1:195353036]><TERRORIST>" [562 2192 -114] killed "Dragomir<12><BOT><CT>" [258 2481 -121] with "glock" (headshot)
     */
    private void parseKill(String content, MatchData matchData, List<String> eventos) {
        // Procura por "killed" no conteúdo
        if (!content.contains(" killed ")) {
            return;
        }
        
        // Pattern para extrair killer, victim, weapon e headshot
        Pattern pattern = Pattern.compile(
            "\"([^<\"]+)<[^>]*>\"\\s+\\[[^\\]]+\\]\\s+killed\\s+\"([^<\"]+)<[^>]*>\"\\s+\\[[^\\]]+\\]\\s+with\\s+\"([^\"]+)\"\\s*(\\(headshot\\))?",
            Pattern.CASE_INSENSITIVE
        );
        
        Matcher matcher = pattern.matcher(content);
        
        if (matcher.find()) {
            String killer = matcher.group(1).trim();
            String victim = matcher.group(2).trim();
            String weapon = matcher.group(3).trim();
            boolean headshot = matcher.group(4) != null && matcher.group(4).toLowerCase().contains("headshot");
            
            String evento = "⚔️ " + killer + " matou " + victim + " com " + weapon + (headshot ? " (💀 HEADSHOT)" : "");
            if (eventos == null) {
                eventos = new ArrayList<>();
                matchData.setEventosRecentes(eventos);
            }
            eventos.add(evento);
            
            // Atualiza informações da última kill para exibição dedicada na UI
            matchData.setUltimaKillKiller(killer);
            matchData.setUltimaKillVictim(victim);
            matchData.setUltimaKillWeapon(weapon);
            matchData.setUltimaKillHeadshot(headshot);
            matchData.setTimestampUltimaKill(System.currentTimeMillis());
        }
    }
    
    /**
     * Parse: "Round_End"
     */
    private void parseRoundEnd(String content, MatchData matchData, List<String> eventos) {
        if (content.contains("World triggered \"Round_End\"")) {
            matchData.setFase("over");
            matchData.setEstadoBomba("none");
            matchData.setTimestampBombaPlantada(0);
            
            // Extrai scores atualizados
            Pattern scorePattern = Pattern.compile("MatchStatus:\\s*Score:\\s*(\\d+):(\\d+)");
            Matcher scoreMatcher = scorePattern.matcher(content);
            if (scoreMatcher.find()) {
                try {
                    int scoreCT = Integer.parseInt(scoreMatcher.group(1));
                    int scoreT = Integer.parseInt(scoreMatcher.group(2));
                    matchData.setScoreCT(scoreCT);
                    matchData.setScoreT(scoreT);
                    
                    String evento = "🏁 Round finalizado - Placar: CT " + scoreCT + " x " + scoreT + " T";
                    if (eventos == null) {
                        eventos = new ArrayList<>();
                        matchData.setEventosRecentes(eventos);
                    }
                    eventos.add(evento);
                    
                    // Limpa bomba ao finalizar round
                    matchData.setEstadoBomba("none");
                    matchData.setTimestampBombaPlantada(0);
                } catch (NumberFormatException e) {
                    // Ignora
                }
            }
        }
    }
}

