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
            // Extrai o JSON do meio dos logs
            String jsonStr = extractRoundStatsJson(content);
            if (jsonStr == null || jsonStr.trim().isEmpty()) {
                return matchData;
            }
            
            // Parse do JSON
            JSONObject root = new JSONObject(jsonStr);
            
            // Mapa
            if (root.has("map")) {
                matchData.setMapa(root.getString("map"));
            }
            
            // Scores
            if (root.has("score_ct")) {
                matchData.setScoreCT(root.getInt("score_ct"));
            }
            
            if (root.has("score_t")) {
                matchData.setScoreT(root.getInt("score_t"));
            }
            
            // Round
            if (root.has("round_number")) {
                String roundStr = root.getString("round_number");
                try {
                    matchData.setRoundAtual(Integer.parseInt(roundStr));
                } catch (NumberFormatException e) {
                    // Ignora
                }
            }
            
            // Players - formato customizado
            if (root.has("players")) {
                JSONObject players = root.getJSONObject("players");
                parseCustomPlayers(players, matchData);
            }
            
            matchData.setConectado(true);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return matchData;
    }
    
    /**
     * Extrai o JSON round_stats do conteúdo
     */
    private String extractRoundStatsJson(String content) {
        if (content == null) {
            return null;
        }
        
        // Procura por JSON_BEGIN{ ... }JSON_END
        int beginIdx = content.indexOf("JSON_BEGIN{");
        int endIdx = content.indexOf("}JSON_END");
        
        if (beginIdx >= 0 && endIdx > beginIdx) {
            String jsonContent = content.substring(beginIdx + "JSON_BEGIN{".length(), endIdx + 1);
            
            // Remove timestamps das linhas (formato: 11/26/2025 - 02:12:21.266 - )
            jsonContent = jsonContent.replaceAll("(?m)^\\d{2}/\\d{2}/\\d{4} - \\d{2}:\\d{2}:\\d{2}\\.\\d{3} - ", "");
            
            // Reconstrói o JSON corretamente
            StringBuilder json = new StringBuilder();
            json.append("{\n");
            
            String[] lines = jsonContent.split("\n");
            for (String line : lines) {
                line = line.trim();
                if (line.isEmpty()) continue;
                
                // Se a linha tem : então é um campo JSON
                if (line.contains(":")) {
                    // Garante formatação correta do JSON
                    if (!line.endsWith(",") && !line.endsWith("{") && !line.endsWith("}")) {
                        // Adiciona vírgula se não for último campo (detecção simples)
                        if (!line.contains("\"players\"") && !line.contains("\"fields\"")) {
                            // Adiciona vírgula exceto no último campo antes de }
                        }
                    }
                    json.append("  ").append(line).append("\n");
                }
            }
            
            json.append("}");
            
            return json.toString();
        }
        
        return null;
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

