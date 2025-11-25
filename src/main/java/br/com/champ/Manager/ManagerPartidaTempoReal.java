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
    
    @Inject
    private ManagerRcon managerRcon;

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
            
            System.out.println("=== DEBUG: Resultado get5_status ===");
            System.out.println(resultado);
            System.out.println("=====================================");
            
            if (resultado != null && !resultado.startsWith("ERRO") && resultado.contains("{")) {
                // Parse JSON do Get5
                parseGet5Status(resultado);
            } else {
                // Fallback para comando status padrão
                resultado = rconService.executeCommand("status");
                
                System.out.println("=== DEBUG: Resultado status ===");
                System.out.println(resultado);
                System.out.println("===============================");
                
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
                
                // Mapa - formato: "loaded spawngroup(  1)  : SV:  [1: de_dust2 | main lump | mapload]"
                if (linha.contains("loaded spawngroup") && linha.contains("SV:")) {
                    Pattern mapPattern = Pattern.compile("\\[\\d+:\\s*([a-z0-9_]+)");
                    Matcher mapMatcher = mapPattern.matcher(linha);
                    if (mapMatcher.find()) {
                        String mapa = mapMatcher.group(1).trim();
                        if (!mapa.isEmpty() && !mapa.equals("prefabs") && !mapa.equals("maps")) {
                            matchData.setMapa(mapa);
                            System.out.println("Mapa encontrado: " + mapa);
                        }
                    }
                }
                
                // Mapa alternativo - formato: "map     : de_dust2"
                // Mas só se ainda não encontrou o mapa e se não for "os/type" ou outros campos
                if (matchData.getMapa() == null || matchData.getMapa().isEmpty() || matchData.getMapa().equals("load")) {
                    if (linha.toLowerCase().startsWith("map") && linha.contains(":")) {
                        Pattern mapPattern = Pattern.compile("(?i)^map\\s*:?\\s*([a-z0-9_]+)", Pattern.CASE_INSENSITIVE);
                        Matcher mapMatcher = mapPattern.matcher(linha);
                        if (mapMatcher.find()) {
                            String mapa = mapMatcher.group(1).trim();
                            if (!mapa.isEmpty() && !mapa.equals("version") && !mapa.equals("hostname") && !mapa.equals("load") && !mapa.equals("s")) {
                                matchData.setMapa(mapa);
                                System.out.println("Mapa encontrado (alternativo): " + mapa);
                            }
                        }
                    }
                }
                
                // Score - formato: "CT: 5  T: 3" (pode não estar no status padrão)
                // Tenta obter com comando específico depois
                if (linha.contains("CT:") || linha.contains("T:")) {
                    Pattern scorePattern = Pattern.compile("CT:\\s*(\\d+).*?T:\\s*(\\d+)", Pattern.CASE_INSENSITIVE);
                    Matcher m = scorePattern.matcher(linha);
                    if (m.find()) {
                        matchData.setScoreCT(Integer.parseInt(m.group(1)));
                        matchData.setScoreT(Integer.parseInt(m.group(2)));
                        System.out.println("Score encontrado: CT=" + m.group(1) + " T=" + m.group(2));
                    } else {
                        // Tenta separado
                        if (linha.contains("CT:")) {
                            Pattern ctPattern = Pattern.compile("CT:\\s*(\\d+)", Pattern.CASE_INSENSITIVE);
                            Matcher ctMatcher = ctPattern.matcher(linha);
                            if (ctMatcher.find()) {
                                matchData.setScoreCT(Integer.parseInt(ctMatcher.group(1)));
                            }
                        }
                        if (linha.contains("T:")) {
                            Pattern tPattern = Pattern.compile("T:\\s*(\\d+)", Pattern.CASE_INSENSITIVE);
                            Matcher tMatcher = tPattern.matcher(linha);
                            if (tMatcher.find()) {
                                matchData.setScoreT(Integer.parseInt(tMatcher.group(1)));
                            }
                        }
                    }
                }
                
                // Round - formato: "Round: 5"
                if (linha.toLowerCase().contains("round")) {
                    Pattern roundPattern = Pattern.compile("(?i)round\\s*:?\\s*(\\d+)", Pattern.CASE_INSENSITIVE);
                    Matcher m = roundPattern.matcher(linha);
                    if (m.find()) {
                        matchData.setRoundAtual(Integer.parseInt(m.group(1)));
                        System.out.println("Round encontrado: " + m.group(1));
                    }
                }
                
                // Detecta início da seção de players
                if (linha.contains("id     time ping loss") || linha.contains("---------players--------")) {
                    naSecaoPlayers = true;
                    continue;
                }
                
                // Detecta fim da seção de players
                if (linha.equals("#end") || linha.equals("")) {
                    naSecaoPlayers = false;
                    continue;
                }
                
                // Players - formato: "   4    06:55   24    0     active 786432 189.13.25.48:55852 'Dede'"
                // ou: "   7      BOT    0    0     active      0 'Arno'"
                // ou: "65535 [NoChan]    0    0 challenging      0unknown ''"
                if (naSecaoPlayers && linha.matches("^\\s*\\d+.*")) {
                    PlayerInfo player = parsePlayerLine(linha);
                    if (player != null && player.getNome() != null && !player.getNome().isEmpty()) {
                        if (player.getTime() == 2) {
                            playersCT.add(player);
                        } else if (player.getTime() == 3) {
                            playersT.add(player);
                        } else {
                            // Se não tem time definido, adiciona temporariamente em CT
                            // Será movido para o time correto quando identificado
                            player.setTime(0); // 0 = não definido
                            playersCT.add(player);
                            System.out.println("Player adicionado sem time: " + player.getNome());
                        }
                    }
                }
            }
            
            matchData.setPlayersCT(playersCT);
            matchData.setPlayersT(playersT);
            
            System.out.println("Players CT: " + playersCT.size());
            System.out.println("Players T: " + playersT.size());
            
            // Debug: lista todos os players encontrados
            for (PlayerInfo p : playersCT) {
                System.out.println("Player CT: " + p.getNome() + " (time=" + p.getTime() + ", alive=" + p.isAlive() + ")");
            }
            for (PlayerInfo p : playersT) {
                System.out.println("Player T: " + p.getNome() + " (time=" + p.getTime() + ", alive=" + p.isAlive() + ")");
            }
            
            // Se não encontrou o mapa, tenta comando específico
            if (matchData.getMapa() == null || matchData.getMapa().isEmpty() || matchData.getMapa().equals("load")) {
                try {
                    String mapResult = rconService.executeCommand("get5_current_map");
                    if (mapResult != null && !mapResult.startsWith("ERRO") && !mapResult.contains("Unknown command")) {
                        matchData.setMapa(mapResult.trim());
                    } else {
                        // Tenta outro comando
                        mapResult = rconService.executeCommand("host_map");
                        if (mapResult != null && !mapResult.startsWith("ERRO")) {
                            matchData.setMapa(mapResult.trim());
                        }
                    }
                } catch (Exception e) {
                    // Ignora
                }
            }
            
            // Tenta obter score se não encontrou
            if (matchData.getScoreCT() == 0 && matchData.getScoreT() == 0) {
                try {
                    // Tenta vários comandos para obter o score
                    String scoreCT = rconService.executeCommand("mp_teamscore_1");
                    String scoreT = rconService.executeCommand("mp_teamscore_2");
                    
                    if (scoreCT != null && !scoreCT.startsWith("ERRO")) {
                        try {
                            // Remove "mp_teamscore_1" = " do resultado se houver
                            scoreCT = scoreCT.replaceAll(".*=.*?\"?([0-9]+).*", "$1");
                            matchData.setScoreCT(Integer.parseInt(scoreCT.trim()));
                        } catch (Exception e) {
                            // Ignora
                        }
                    }
                    
                    if (scoreT != null && !scoreT.startsWith("ERRO")) {
                        try {
                            scoreT = scoreT.replaceAll(".*=.*?\"?([0-9]+).*", "$1");
                            matchData.setScoreT(Integer.parseInt(scoreT.trim()));
                        } catch (Exception e) {
                            // Ignora
                        }
                    }
                    
                    // Se ainda não conseguiu, tenta get5_status
                    if (matchData.getScoreCT() == 0 && matchData.getScoreT() == 0) {
                        String scoreResult = rconService.executeCommand("get5_status");
                        if (scoreResult != null && !scoreResult.startsWith("ERRO") && !scoreResult.contains("Unknown command")) {
                            // Se retornou JSON, tenta parsear
                            if (scoreResult.contains("{")) {
                                parseGet5Status(scoreResult);
                            }
                        }
                    }
                } catch (Exception e) {
                    // Ignora
                }
            }
            
            // Tenta obter informações detalhadas dos players usando outros comandos
            atualizarInformacoesAdicionais();
            
            // Tenta identificar o time dos players que não têm time definido
            // usando o comando get5_listplayers ou similar
            identificarTimesPlayers();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Parse de uma linha de player do comando status
     * Formato: "   4    06:55   24    0     active 786432 189.13.25.48:55852 'Dede'"
     * ou: "   7      BOT    0    0     active      0 'Arno'"
     * ou: "65535 [NoChan]    0    0 challenging      0unknown ''"
     * 
     * NOTA: O segundo campo NÃO é o time do jogo, é tempo de conexão ou "BOT"
     * O time precisa ser obtido de outra forma (comando adicional ou inferência)
     */
    private PlayerInfo parsePlayerLine(String linha) {
        try {
            // Ignora linhas vazias ou com [NoChan]
            if (linha.contains("[NoChan]") || linha.contains("''") || linha.trim().isEmpty()) {
                return null;
            }
            
            PlayerInfo player = new PlayerInfo();
            
            // Extrai o nome entre aspas simples (último campo)
            Pattern namePattern = Pattern.compile("'([^']+)'");
            Matcher nameMatcher = namePattern.matcher(linha);
            if (nameMatcher.find()) {
                String nome = nameMatcher.group(1).trim();
                if (nome.isEmpty()) {
                    return null; // Ignora players sem nome
                }
                player.setNome(nome);
            } else {
                return null; // Se não tem nome, ignora
            }
            
            // Divide a linha em campos (separados por múltiplos espaços)
            // Formato: id time ping loss state rate adr name
            String[] partes = linha.trim().split("\\s+");
            
            if (partes.length < 3) {
                return null;
            }
            
            // ID é o primeiro campo
            String idStr = partes[0];
            try {
                int id = Integer.parseInt(idStr);
                if (id == 65535) {
                    return null; // Ignora slots vazios
                }
            } catch (NumberFormatException e) {
                return null; // Se não começa com número, não é um player válido
            }
            
            // O segundo campo pode ser:
            // - "BOT" (bot)
            // - Formato de tempo "06:55" (tempo de conexão, não time do jogo)
            // - Número 2 ou 3 (time do jogo - raro neste formato)
            String secondField = partes[1];
            
            // Tenta identificar o time - mas geralmente não está neste campo
            if (secondField.equals("2")) {
                player.setTime(2); // CT
            } else if (secondField.equals("3")) {
                player.setTime(3); // T
            } else if (secondField.equals("BOT")) {
                // Bot - não sabemos o time ainda
                player.setSteamId("BOT");
            }
            // Se não encontrou, deixa sem time (será identificado depois)
            
            // Tenta extrair Steam ID se houver (formato IP:porta pode conter)
            Pattern steamPattern = Pattern.compile("(STEAM_[0-9]:[0-9]:[0-9]+|\\[U:[0-9]+:[0-9]+\\]|\\[G:[0-9]+:[0-9]+\\])");
            Matcher steamMatcher = steamPattern.matcher(linha);
            if (steamMatcher.find()) {
                player.setSteamId(steamMatcher.group(1));
            }
            
            // Estado - se está "active", está vivo
            if (linha.contains("active")) {
                player.setAlive(true);
                player.setHealth(100); // Assume 100 se está ativo (pode ser melhorado depois)
            } else if (linha.contains("challenging")) {
                player.setAlive(false);
                player.setHealth(0);
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
     * Tenta identificar o time dos players que não têm time definido
     * Usa vários comandos para tentar obter essa informação
     */
    private void identificarTimesPlayers() {
        try {
            // Tenta usar o comando get5_listplayers
            String result = rconService.executeCommand("get5_listplayers");
            if (result != null && !result.startsWith("ERRO") && !result.contains("Unknown command")) {
                parsePlayerTeams(result);
            }
            
            // Tenta usar o comando status novamente procurando por informações de time
            // ou usa outros comandos disponíveis
            result = rconService.executeCommand("listplayers");
            if (result != null && !result.startsWith("ERRO") && !result.contains("Unknown command")) {
                parsePlayerTeams(result);
            }
            
            // Se ainda não identificou, tenta inferir baseado na ordem ou outros critérios
            // Por enquanto, deixa sem time definido - será mostrado em ambos os times ou em um genérico
        } catch (Exception e) {
            // Ignora erros
        }
    }
    
    /**
     * Parse de informações de times dos players de vários formatos
     */
    private void parsePlayerTeams(String result) {
        String[] linhas = result.split("\n");
        for (String linha : linhas) {
            linha = linha.trim();
            
            // Procura por informações de time
            if (linha.contains("CT") || linha.contains("Counter-Terrorist") || linha.contains("Counter Terrorist")) {
                atualizarTimePlayers(linha, 2);
            } else if (linha.contains(" T ") || linha.contains("Terrorist") || linha.matches(".*\\bT\\b.*")) {
                atualizarTimePlayers(linha, 3);
            }
            
            // Tenta padrões como "Player Name (CT)" ou "Player Name (T)"
            Pattern teamPattern = Pattern.compile("'([^']+)'\\s*\\(?(CT|T|Counter-Terrorist|Terrorist)\\)?", Pattern.CASE_INSENSITIVE);
            Matcher teamMatcher = teamPattern.matcher(linha);
            if (teamMatcher.find()) {
                String nome = teamMatcher.group(1);
                String timeStr = teamMatcher.group(2).toUpperCase();
                int time = (timeStr.contains("CT") || timeStr.contains("COUNTER")) ? 2 : 3;
                
                // Atualiza o time do player
                for (PlayerInfo player : matchData.getPlayersCT()) {
                    if (player.getNome().equals(nome)) {
                        if (time == 3) {
                            matchData.getPlayersCT().remove(player);
                            matchData.getPlayersT().add(player);
                            player.setTime(3);
                        } else {
                            player.setTime(2);
                        }
                        return;
                    }
                }
                for (PlayerInfo player : matchData.getPlayersT()) {
                    if (player.getNome().equals(nome)) {
                        if (time == 2) {
                            matchData.getPlayersT().remove(player);
                            matchData.getPlayersCT().add(player);
                            player.setTime(2);
                        } else {
                            player.setTime(3);
                        }
                        return;
                    }
                }
            }
        }
    }
    
    /**
     * Atualiza o time de um player baseado em informações adicionais
     */
    private void atualizarTimePlayers(String linha, int time) {
        // Tenta encontrar o nome ou steamid na linha
        Pattern namePattern = Pattern.compile("'([^']+)'");
        Matcher nameMatcher = namePattern.matcher(linha);
        if (nameMatcher.find()) {
            String nome = nameMatcher.group(1);
            
            // Procura nos players CT
            List<PlayerInfo> playersCTCopy = new ArrayList<>(matchData.getPlayersCT());
            for (PlayerInfo player : playersCTCopy) {
                if (player.getNome().equals(nome)) {
                    if (time == 2) {
                        player.setTime(2);
                    } else if (time == 3) {
                        // Move para T
                        matchData.getPlayersCT().remove(player);
                        matchData.getPlayersT().add(player);
                        player.setTime(3);
                    }
                    return;
                }
            }
            
            // Procura nos players T
            List<PlayerInfo> playersTCopy = new ArrayList<>(matchData.getPlayersT());
            for (PlayerInfo player : playersTCopy) {
                if (player.getNome().equals(nome)) {
                    if (time == 3) {
                        player.setTime(3);
                    } else if (time == 2) {
                        // Move para CT
                        matchData.getPlayersT().remove(player);
                        matchData.getPlayersCT().add(player);
                        player.setTime(2);
                    }
                    return;
                }
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
     * Testa a conexão RCON
     */
    public void testarConexao() {
        if (managerRcon != null) {
            managerRcon.testarConexao();
        }
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

