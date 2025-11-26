/*
 * Parser para formato customizado de round_stats do CS2
 * Este formato é diferente do GSI padrão da Valve
 */
package br.com.champ.Servico;

import br.com.champ.vo.MatchData;
import br.com.champ.vo.PlayerInfo;
import jakarta.ejb.Stateless;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parser para o formato customizado round_stats que o CS2 está enviando
 */
@Stateless
public class CustomRoundStatsParser {
    
    /**
     * Parse do formato customizado round_stats
     */
    public MatchData parseRoundStats(String content) {
        MatchData matchData = new MatchData();
        
        try {
            // Primeiro tenta extrair campos diretamente sem reconstruir JSON completo
            extractFieldsDirectly(content, matchData);
            
            // Tenta extrair players do JSON completo (se conseguir)
            // Os campos principais já foram extraídos diretamente acima
            String jsonStr = extractRoundStatsJson(content);
            boolean playersParsed = false;
            
            if (jsonStr != null && !jsonStr.trim().isEmpty()) {
                String trimmed = jsonStr.trim();
                // Só tenta parsear se for um JSON válido
                if (trimmed.startsWith("{") && trimmed.endsWith("}")) {
                    try {
                        JSONObject root = new JSONObject(jsonStr);
                        
                        // Tenta pegar players do JSON
                        if (root.has("players")) {
                            JSONObject players = root.getJSONObject("players");
                            parseCustomPlayers(players, matchData);
                            playersParsed = true;
                        }
                    } catch (Exception e) {
                        System.out.println("Erro ao parsear JSON completo para players: " + e.getMessage());
                        // Vai tentar extrair diretamente abaixo
                    }
                }
            }
            
            // Se não conseguiu parsear players do JSON, extrai diretamente do conteúdo
            if (!playersParsed) {
                extractPlayersDirectly(content, matchData);
            }
            
            matchData.setConectado(true);
            
        } catch (Exception e) {
            System.out.println("Erro no parseRoundStats: " + e.getMessage());
            e.printStackTrace();
        }
        
        return matchData;
    }
    
    /**
     * Extrai campos diretamente do conteúdo usando regex (mais confiável)
     */
    private void extractFieldsDirectly(String content, MatchData matchData) {
        try {
            // Mapa - só atualiza se ainda não estiver definido ou se mudar
            Pattern pattern = Pattern.compile("\"map\"\\s*:\\s*\"([^\"]+)\"");
            Matcher matcher = pattern.matcher(content);
            if (matcher.find()) {
                String novoMapa = matcher.group(1).trim();
                String mapaAtual = matchData.getMapa();
                
                // Só atualiza se:
                // 1. Não há mapa definido ainda, OU
                // 2. O mapa mudou (diferente do atual)
                if (mapaAtual == null || mapaAtual.isEmpty() || !mapaAtual.equals(novoMapa)) {
                    matchData.setMapa(novoMapa);
                    System.out.println("=== MAPA ATUALIZADO: " + novoMapa + " ===");
                }
            }
            
            // Round number
            pattern = Pattern.compile("\"round_number\"\\s*:\\s*\"?(\\d+)\"?");
            matcher = pattern.matcher(content);
            if (matcher.find()) {
                try {
                    matchData.setRoundAtual(Integer.parseInt(matcher.group(1)));
                } catch (NumberFormatException e) {
                    // Ignora
                }
            }
            
            // Score CT
            pattern = Pattern.compile("\"score_ct\"\\s*:\\s*\"?(\\d+)\"?");
            matcher = pattern.matcher(content);
            if (matcher.find()) {
                try {
                    matchData.setScoreCT(Integer.parseInt(matcher.group(1)));
                } catch (NumberFormatException e) {
                    // Ignora
                }
            }
            
            // Score T
            pattern = Pattern.compile("\"score_t\"\\s*:\\s*\"?(\\d+)\"?");
            matcher = pattern.matcher(content);
            if (matcher.find()) {
                try {
                    matchData.setScoreT(Integer.parseInt(matcher.group(1)));
                } catch (NumberFormatException e) {
                    // Ignora
                }
            }
        } catch (Exception e) {
            System.out.println("Erro ao extrair campos diretamente: " + e.getMessage());
        }
    }
    
    /**
     * Extrai players diretamente do conteúdo usando regex
     */
    private void extractPlayersDirectly(String content, MatchData matchData) {
        List<PlayerInfo> playersCT = new ArrayList<>();
        List<PlayerInfo> playersT = new ArrayList<>();
        
        try {
            // Procura por "player_X" : "dados..."
            Pattern playerPattern = Pattern.compile("\"player_(\\d+)\"\\s*:\\s*\"([^\"]+)\"");
            Matcher playerMatcher = playerPattern.matcher(content);
            
            while (playerMatcher.find()) {
                String playerData = playerMatcher.group(2);
                PlayerInfo player = parseCustomPlayerData(playerData);
                
                if (player != null) {
                    if (player.getTime() == 2) {
                        playersCT.add(player);
                    } else if (player.getTime() == 3) {
                        playersT.add(player);
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Erro ao extrair players diretamente: " + e.getMessage());
        }
        
        matchData.setPlayersCT(playersCT);
        matchData.setPlayersT(playersT);
    }
    
    /**
     * Extrai o JSON round_stats do conteúdo e reconstrói um JSON válido
     */
    private String extractRoundStatsJson(String content) {
        if (content == null) {
            return null;
        }
        
        // Procura por JSON_BEGIN{ ... }JSON_END
        int beginIdx = content.indexOf("JSON_BEGIN{");
        int endIdx = content.indexOf("}JSON_END");
        
        if (beginIdx < 0 || endIdx <= beginIdx) {
            return null;
        }
        
        String jsonContent = content.substring(beginIdx + "JSON_BEGIN{".length(), endIdx + 1);
        
        // Remove timestamps das linhas (formato: 11/26/2025 - 02:12:21.266 - )
        jsonContent = jsonContent.replaceAll("(?m)^\\d{2}/\\d{2}/\\d{4} - \\d{2}:\\d{2}:\\d{2}\\.\\d{3} - ", "");
        
        // Reconstrói o JSON corretamente, linha por linha
        StringBuilder json = new StringBuilder();
        json.append("{\n");
        
        String[] lines = jsonContent.split("\n");
        List<String> processedLines = new ArrayList<>();
        
        // Primeiro, coleta todas as linhas válidas
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) continue;
            processedLines.add(line);
        }
        
        // Agora reconstrói o JSON adicionando vírgulas corretamente
        for (int i = 0; i < processedLines.size(); i++) {
            String line = processedLines.get(i);
            String nextLine = (i + 1 < processedLines.size()) ? processedLines.get(i + 1) : null;
            
            // Remove vírgula no final se houver (vamos adicionar depois se necessário)
            line = line.replaceAll(",$", "");
            
            // Verifica se precisa adicionar vírgula
            boolean needsComma = false;
            if (nextLine != null) {
                // Não adiciona vírgula se próxima linha fecha objeto/array
                if (!nextLine.trim().equals("}") && !nextLine.trim().equals("}}")) {
                    // Não adiciona vírgula se esta linha abre objeto
                    if (!line.endsWith("{")) {
                        needsComma = true;
                    }
                }
            }
            
            // Adiciona a linha com ou sem vírgula
            json.append("  ").append(line);
            if (needsComma) {
                json.append(",");
            }
            json.append("\n");
        }
        
        json.append("}");
        
        String result = json.toString();
        
        // Valida se é um JSON válido antes de retornar
        try {
            // Valida que começa e termina corretamente
            result = result.trim();
            if (!result.startsWith("{") || !result.endsWith("}")) {
                return null;
            }
            
            // Tenta parsear para validar
            new JSONObject(result);
            return result;
        } catch (Exception e) {
            System.out.println("JSON reconstruído não é válido: " + e.getMessage());
            // Retorna null em vez de tentar buildSimpleJsonFromContent
            // Os campos já foram extraídos diretamente no método principal
            return null;
        }
    }
    
    /**
     * Parse dos players no formato customizado
     * Formato: "player_2" : "195353036, 2, 16000, 2, 3, 0, 200.00, 100.00, 0.67, 18, 3, 0, 0, 0, 0, 0, 2, 2, 2, 0, 0, 0.000000, 0.000000, 0, 1, 0"
     * Campos: accountid, team, money, kills, deaths, assists, dmg, hsp, kdr, adr, mvp, ef, ud, 3k, 4k, 5k, clutchk, firstk, pistolk, sniperk, blindk, bombk, firedmg, uniquek, dinks, chickenk
     */
    private void parseCustomPlayers(JSONObject playersJson, MatchData matchData) {
        List<PlayerInfo> playersCT = new ArrayList<>();
        List<PlayerInfo> playersT = new ArrayList<>();
        
        try {
            // Campos: accountid, team, money, kills, deaths, assists, dmg, hsp, kdr, adr, mvp, ...
            for (String key : playersJson.keySet()) {
                if (key.startsWith("player_")) {
                    String playerData = playersJson.getString(key);
                    PlayerInfo player = parseCustomPlayerData(playerData);
                    
                    if (player != null) {
                        if (player.getTime() == 2) {
                            playersCT.add(player);
                        } else if (player.getTime() == 3) {
                            playersT.add(player);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        matchData.setPlayersCT(playersCT);
        matchData.setPlayersT(playersT);
    }
    
    /**
     * Parse de uma linha de dados do player no formato customizado
     */
    private PlayerInfo parseCustomPlayerData(String data) {
        PlayerInfo player = new PlayerInfo();
        
        try {
            // Divide por vírgula e remove espaços
            String[] fields = data.split(",");
            if (fields.length < 6) {
                return null;
            }
            
            // accountid (posição 0)
            String accountId = fields[0].trim();
            if (!accountId.isEmpty() && !accountId.equals("0")) {
                player.setSteamId(accountId);
            }
            
            // team (posição 1) - 2 = CT, 3 = T
            String teamStr = fields[1].trim();
            try {
                int team = Integer.parseInt(teamStr);
                player.setTime(team);
            } catch (NumberFormatException e) {
                // Ignora
            }
            
            // kills (posição 3)
            if (fields.length > 3) {
                try {
                    player.setKills(Integer.parseInt(fields[3].trim()));
                } catch (Exception e) {
                    // Ignora
                }
            }
            
            // deaths (posição 4)
            if (fields.length > 4) {
                try {
                    player.setDeaths(Integer.parseInt(fields[4].trim()));
                } catch (Exception e) {
                    // Ignora
                }
            }
            
            // assists (posição 5)
            if (fields.length > 5) {
                try {
                    player.setAssists(Integer.parseInt(fields[5].trim()));
                } catch (Exception e) {
                    // Ignora
                }
            }
            
            // Por enquanto não temos nome, então usamos o accountid
            player.setNome("Player " + accountId);
            
            // Assume que está vivo (health > 0) por padrão
            player.setAlive(true);
            player.setHealth(100);
            
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        
        return player;
    }
}

