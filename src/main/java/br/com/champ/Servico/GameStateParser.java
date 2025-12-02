/*
 * Parser para converter JSON do GSI em MatchData
 */
package br.com.champ.Servico;

import br.com.champ.vo.MatchData;
import br.com.champ.vo.PlayerInfo;
import jakarta.ejb.Stateless;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

/**
 * Classe responsável por fazer o parse do JSON do GSI e converter para MatchData
 */
@Stateless
public class GameStateParser {
    
    /**
     * Parse do JSON do GSI para MatchData
     */
    public MatchData parseGSIJson(String jsonString) {
        MatchData matchData = new MatchData();
        
        // Validação prévia do JSON
        if (jsonString == null || jsonString.trim().isEmpty()) {
            return matchData;
        }
        
        // Garante que começa com { (JSON válido)
        String trimmed = jsonString.trim();
        if (!trimmed.startsWith("{") || !trimmed.endsWith("}")) {
            // Não é um JSON válido
            return matchData;
        }
        
        try {
            JSONObject root = new JSONObject(jsonString);
            
            // Provider - informações básicas
            if (root.has("provider")) {
                // Pode conter informações úteis como appid, steamid, etc
                // Por enquanto não é necessário usar essas informações
            }
            
            // Map - informações do mapa e fase
            if (root.has("map")) {
                JSONObject map = root.getJSONObject("map");
                
                // Nome do mapa
                if (map.has("name")) {
                    matchData.setMapa(map.getString("name"));
                }
                
                // Fase do jogo
                if (map.has("phase")) {
                    matchData.setFase(map.getString("phase"));
                }
                
                // Round atual
                if (map.has("round")) {
                    matchData.setRoundAtual(map.getInt("round"));
                }
                
                // Scores
                if (map.has("team_ct")) {
                    JSONObject teamCT = map.getJSONObject("team_ct");
                    if (teamCT.has("score")) {
                        matchData.setScoreCT(teamCT.getInt("score"));
                    }
                }
                
                if (map.has("team_t")) {
                    JSONObject teamT = map.getJSONObject("team_t");
                    if (teamT.has("score")) {
                        matchData.setScoreT(teamT.getInt("score"));
                    }
                }
            }
            
            // Round - informações do round atual
            if (root.has("round")) {
                JSONObject round = root.getJSONObject("round");
                
                // Fase do round
                if (round.has("phase")) {
                    String phase = round.getString("phase");
                    if (matchData.getFase() == null || matchData.getFase().isEmpty()) {
                        matchData.setFase(phase);
                    }
                }
                
                // Bomb state
                if (round.has("bomb")) {
                    matchData.setEstadoBomba(round.getString("bomb"));
                }
            }
            
            // Phase countdowns - tempo restante
            if (root.has("phase_countdowns")) {
                JSONObject phaseCountdowns = root.getJSONObject("phase_countdowns");
                
                if (phaseCountdowns.has("phase_ends_in")) {
                    String phaseEndsIn = phaseCountdowns.getString("phase_ends_in");
                    try {
                        double seconds = Double.parseDouble(phaseEndsIn);
                        matchData.setTempoRound((int) seconds);
                    } catch (NumberFormatException e) {
                        // Ignora se não for número
                    }
                }
            }
            
            // Bomb - informações da bomba
            if (root.has("bomb")) {
                JSONObject bomb = root.getJSONObject("bomb");
                
                if (bomb.has("state")) {
                    matchData.setEstadoBomba(bomb.getString("state"));
                }
                
                if (bomb.has("countdown")) {
                    String countdown = bomb.getString("countdown");
                    try {
                        double seconds = Double.parseDouble(countdown);
                        matchData.setTempoBomba((int) seconds);
                    } catch (NumberFormatException e) {
                        // Ignora se não for número
                    }
                }
            }
            
            // Players - informações dos jogadores
            parsePlayers(root, matchData);
            
            matchData.setConectado(true);
            
        } catch (Exception e) {
            e.printStackTrace();
            matchData.setConectado(false);
        }
        
        return matchData;
    }
    
    /**
     * Parse das informações dos jogadores
     */
    private void parsePlayers(JSONObject root, MatchData matchData) {
        List<PlayerInfo> playersCT = new ArrayList<>();
        List<PlayerInfo> playersT = new ArrayList<>();
        
        try {
            // Allplayers - todos os jogadores (válido para espectadores/GOTV)
            if (root.has("allplayers")) {
                JSONObject allPlayers = root.getJSONObject("allplayers");
                
                for (String key : allPlayers.keySet()) {
                    JSONObject playerJson = allPlayers.getJSONObject(key);
                    PlayerInfo player = parsePlayer(playerJson);
                    
                    if (player != null && player.getNome() != null && !player.getNome().isEmpty()) {
                        if (player.getTime() == 2) {
                            playersCT.add(player);
                        } else if (player.getTime() == 3) {
                            playersT.add(player);
                        }
                    }
                }
            }
            
            // Player - jogador atual (se não tiver allplayers)
            if (playersCT.isEmpty() && playersT.isEmpty() && root.has("player")) {
                JSONObject playerJson = root.getJSONObject("player");
                PlayerInfo player = parsePlayer(playerJson);
                
                if (player != null) {
                    if (player.getTime() == 2) {
                        playersCT.add(player);
                    } else if (player.getTime() == 3) {
                        playersT.add(player);
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
     * Parse de um objeto player individual
     */
    private PlayerInfo parsePlayer(JSONObject playerJson) {
        PlayerInfo player = new PlayerInfo();
        
        try {
            // Steam ID
            if (playerJson.has("steamid")) {
                player.setSteamId(playerJson.getString("steamid"));
            }
            
            // Nome
            if (playerJson.has("name")) {
                player.setNome(playerJson.getString("name"));
            }
            
            // Time
            if (playerJson.has("team")) {
                String team = playerJson.getString("team");
                if ("CT".equalsIgnoreCase(team) || "counter-terrorist".equalsIgnoreCase(team)) {
                    player.setTime(2); // CT
                } else if ("T".equalsIgnoreCase(team) || "terrorist".equalsIgnoreCase(team)) {
                    player.setTime(3); // T
                }
            }
            
            // State - estado do jogador
            if (playerJson.has("state")) {
                JSONObject state = playerJson.getJSONObject("state");
                
                if (state.has("health")) {
                    player.setHealth(state.getInt("health"));
                    player.setAlive(state.getInt("health") > 0);
                }
                
                if (state.has("armor")) {
                    player.setArmor(state.getInt("armor"));
                }
                
                // helmet está disponível mas não é necessário para o MatchData atual
                // if (state.has("helmet")) {
                //     boolean helmet = state.getBoolean("helmet");
                // }
            }
            
            // Match stats - estatísticas da partida
            if (playerJson.has("match_stats")) {
                JSONObject matchStats = playerJson.getJSONObject("match_stats");
                
                if (matchStats.has("kills")) {
                    player.setKills(matchStats.getInt("kills"));
                }
                
                if (matchStats.has("deaths")) {
                    player.setDeaths(matchStats.getInt("deaths"));
                }
                
                if (matchStats.has("assists")) {
                    player.setAssists(matchStats.getInt("assists"));
                }
            }
            
            // Weapons - armas do jogador
            if (playerJson.has("weapons")) {
                JSONObject weapons = playerJson.getJSONObject("weapons");
                // Pega a arma ativa
                for (String key : weapons.keySet()) {
                    JSONObject weapon = weapons.getJSONObject(key);
                    if (weapon.has("state") && "active".equalsIgnoreCase(weapon.getString("state"))) {
                        if (weapon.has("name")) {
                            player.setWeapon(weapon.getString("name"));
                        }
                        break;
                    }
                }
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        
        return player;
    }
}

