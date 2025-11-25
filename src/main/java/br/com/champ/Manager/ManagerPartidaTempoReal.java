/*
 * Manager para coletar dados da partida em tempo real via RCON
 */
package br.com.champ.Manager;

import br.com.champ.Servico.RconService;
import br.com.champ.vo.MatchData;
import br.com.champ.vo.PlayerInfo;
import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Manager para monitorar partida em tempo real
 */
@Named
@ViewScoped
public class ManagerPartidaTempoReal extends ManagerBase {

    @Inject
    private RconService rconService;

    private MatchData matchData;
    private boolean monitorando;
    
    @PostConstruct
    public void init() {
        matchData = new MatchData();
        monitorando = false;
        atualizarDados();
    }
    
    /**
     * Atualiza os dados da partida via RCON
     */
    public void atualizarDados() {
        try {
            // Primeiro tenta get5_status (se o servidor tiver Get5)
            String resultado = rconService.executeCommand("get5_status");
            
            if (resultado != null && !resultado.startsWith("ERRO") && resultado.contains("{")) {
                // Parse JSON do Get5
                parseGet5Status(resultado);
            } else {
                // Fallback para comando status padrão
                resultado = rconService.executeCommand("status");
                if (resultado != null && !resultado.startsWith("ERRO")) {
                    parseStatus(resultado);
                } else {
                    matchData.setConectado(false);
                    return;
                }
            }
            
            matchData.setConectado(true);
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
            matchData.setUltimaAtualizacao(sdf.format(new Date()));
            
        } catch (Exception e) {
            e.printStackTrace();
            matchData.setConectado(false);
        }
    }
    
    /**
     * Parse do resultado do comando get5_status (JSON)
     */
    private void parseGet5Status(String json) {
        try {
            // Extrai informações básicas do JSON
            // Nota: Para parsing completo de JSON, seria melhor usar uma biblioteca como Jackson
            // Por enquanto, vamos usar regex para extrair informações básicas
            
            // Score
            Pattern scorePattern = Pattern.compile("\"score\":\\s*\\{\\s*\"(ct|t)\":\\s*(\\d+)");
            Matcher scoreMatcher = scorePattern.matcher(json);
            while (scoreMatcher.find()) {
                String time = scoreMatcher.group(1);
                int score = Integer.parseInt(scoreMatcher.group(2));
                if ("ct".equals(time)) {
                    matchData.setScoreCT(score);
                } else {
                    matchData.setScoreT(score);
                }
            }
            
            // Round time
            Pattern roundTimePattern = Pattern.compile("\"round_time_remaining\":\\s*(\\d+)");
            Matcher roundTimeMatcher = roundTimePattern.matcher(json);
            if (roundTimeMatcher.find()) {
                matchData.setTempoRound(Integer.parseInt(roundTimeMatcher.group(1)));
            }
            
            // Bomb state
            Pattern bombPattern = Pattern.compile("\"bomb\":\\s*\"([^\"]+)\"");
            Matcher bombMatcher = bombPattern.matcher(json);
            if (bombMatcher.find()) {
                matchData.setEstadoBomba(bombMatcher.group(1));
            }
            
            // Players
            parsePlayersFromGet5(json);
            
        } catch (Exception e) {
            e.printStackTrace();
            // Se falhar, tenta o método status padrão
            String status = rconService.executeCommand("status");
            if (status != null) {
                parseStatus(status);
            }
        }
    }
    
    /**
     * Parse do resultado do comando status padrão
     */
    private void parseStatus(String status) {
        try {
            List<PlayerInfo> playersCT = new ArrayList<>();
            List<PlayerInfo> playersT = new ArrayList<>();
            
            // Extrai informações do status
            String[] linhas = status.split("\n");
            boolean naSecaoPlayers = false;
            
            for (String linha : linhas) {
                linha = linha.trim();
                
                // Score
                if (linha.contains("CT:")) {
                    Pattern scorePattern = Pattern.compile("CT:\\s*(\\d+)");
                    Matcher m = scorePattern.matcher(linha);
                    if (m.find()) {
                        matchData.setScoreCT(Integer.parseInt(m.group(1)));
                    }
                }
                if (linha.contains("T:")) {
                    Pattern scorePattern = Pattern.compile("T:\\s*(\\d+)");
                    Matcher m = scorePattern.matcher(linha);
                    if (m.find()) {
                        matchData.setScoreT(Integer.parseInt(m.group(1)));
                    }
                }
                
                // Round
                if (linha.contains("Round:")) {
                    Pattern roundPattern = Pattern.compile("Round:\\s*(\\d+)");
                    Matcher m = roundPattern.matcher(linha);
                    if (m.find()) {
                        matchData.setRoundAtual(Integer.parseInt(m.group(1)));
                    }
                }
                
                // Mapa
                if (linha.contains("map :")) {
                    String[] partes = linha.split(":");
                    if (partes.length > 1) {
                        matchData.setMapa(partes[1].trim());
                    }
                }
                
                // Players - formato: "# userid name uniqueid connected ping loss state rate adr"
                if (linha.matches("^#\\s+\\d+.*")) {
                    naSecaoPlayers = true;
                    PlayerInfo player = parsePlayerLine(linha);
                    if (player != null) {
                        if (player.getTime() == 2) {
                            playersCT.add(player);
                        } else if (player.getTime() == 3) {
                            playersT.add(player);
                        }
                    }
                } else if (naSecaoPlayers && linha.isEmpty()) {
                    naSecaoPlayers = false;
                }
            }
            
            matchData.setPlayersCT(playersCT);
            matchData.setPlayersT(playersT);
            
            // Tenta obter mais informações com outros comandos
            atualizarInformacoesAdicionais();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Parse de uma linha de player do comando status
     */
    private PlayerInfo parsePlayerLine(String linha) {
        try {
            // Formato: "# userid name uniqueid connected ping loss state rate adr"
            String[] partes = linha.split("\\s+");
            if (partes.length < 3) {
                return null;
            }
            
            PlayerInfo player = new PlayerInfo();
            
            // name (pode ter espaços)
            StringBuilder nome = new StringBuilder();
            int i = 2;
            while (i < partes.length && !partes[i].startsWith("STEAM_") && !partes[i].startsWith("[U:")) {
                if (nome.length() > 0) {
                    nome.append(" ");
                }
                nome.append(partes[i]);
                i++;
            }
            player.setNome(nome.toString());
            
            // steamid
            if (i < partes.length) {
                player.setSteamId(partes[i]);
            }
            
            // state (contém informações sobre time, health, etc)
            // Formato: "state" pode conter informações como "CT" ou "TERRORIST"
            for (int j = i; j < partes.length; j++) {
                if (partes[j].contains("CT") || partes[j].equals("2")) {
                    player.setTime(2);
                    break;
                } else if (partes[j].contains("TERRORIST") || partes[j].equals("3")) {
                    player.setTime(3);
                    break;
                }
            }
            
            return player;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Atualiza informações adicionais usando outros comandos RCON
     */
    private void atualizarInformacoesAdicionais() {
        try {
            // Tenta obter informações de health, armor, etc usando get5_stats ou outros comandos
            String stats = rconService.executeCommand("get5_stats");
            if (stats != null && !stats.startsWith("ERRO") && stats.contains("{")) {
                parseStats(stats);
            }
        } catch (Exception e) {
            // Ignora erros, dados básicos já foram coletados
        }
    }
    
    /**
     * Parse de stats do Get5
     */
    private void parseStats(String json) {
        try {
            // Extrai health, armor, kills, deaths dos players
            // Implementação simplificada - pode ser melhorada com parser JSON completo
            Pattern playerPattern = Pattern.compile("\"steamid\":\\s*\"([^\"]+)\".*?\"health\":\\s*(\\d+).*?\"armor\":\\s*(\\d+).*?\"kills\":\\s*(\\d+).*?\"deaths\":\\s*(\\d+)");
            Matcher m = playerPattern.matcher(json);
            
            while (m.find()) {
                String steamId = m.group(1);
                int health = Integer.parseInt(m.group(2));
                int armor = Integer.parseInt(m.group(3));
                int kills = Integer.parseInt(m.group(4));
                int deaths = Integer.parseInt(m.group(5));
                
                // Atualiza player correspondente
                atualizarPlayerStats(steamId, health, armor, kills, deaths);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Atualiza estatísticas de um player
     */
    private void atualizarPlayerStats(String steamId, int health, int armor, int kills, int deaths) {
        for (PlayerInfo player : matchData.getPlayersCT()) {
            if (steamId.equals(player.getSteamId())) {
                player.setHealth(health);
                player.setArmor(armor);
                player.setKills(kills);
                player.setDeaths(deaths);
                player.setAlive(health > 0);
                return;
            }
        }
        for (PlayerInfo player : matchData.getPlayersT()) {
            if (steamId.equals(player.getSteamId())) {
                player.setHealth(health);
                player.setArmor(armor);
                player.setKills(kills);
                player.setDeaths(deaths);
                player.setAlive(health > 0);
                return;
            }
        }
    }
    
    /**
     * Parse de players do Get5 JSON
     */
    private void parsePlayersFromGet5(String json) {
        // Implementação simplificada - pode ser melhorada
        // Por enquanto, usa o método status padrão
        String status = rconService.executeCommand("status");
        if (status != null) {
            parseStatus(status);
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

