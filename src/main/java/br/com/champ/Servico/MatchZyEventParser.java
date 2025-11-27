/*
 * Parser para eventos do MatchZy
 * Processa eventos JSON enviados pelo MatchZy via HTTP POST
 */
package br.com.champ.Servico;

import br.com.champ.vo.MatchData;
import br.com.champ.vo.MatchZyEventDTO;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Parser para processar eventos do MatchZy e atualizar o MatchData
 */
@Stateless
public class MatchZyEventParser {
    
    @EJB
    private MatchZyPartidaService matchZyPartidaService;
    
    private static final int MAX_EVENTOS = 20;
    
    /**
     * Processa um evento do MatchZy e atualiza o MatchData
     */
    public void parseEvent(MatchZyEventDTO event, MatchData matchData) {
        if (event == null || event.getEvent() == null) {
            return;
        }
        
        String eventType = event.getEvent();
        List<String> eventos = matchData.getEventosRecentes();
        if (eventos == null) {
            eventos = new ArrayList<>();
            matchData.setEventosRecentes(eventos);
        }
        
        // Processa diferentes tipos de eventos
        switch (eventType.toLowerCase()) {
            case "match_start":
                parseMatchStart(event, matchData, eventos);
                break;
            case "round_start":
                parseRoundStart(event, matchData, eventos);
                break;
            case "round_end":
                parseRoundEnd(event, matchData, eventos);
                break;
            case "round_freeze_end":
                parseRoundFreezeEnd(event, matchData, eventos);
                break;
            case "going_live":
                parseGoingLive(event, matchData, eventos);
                break;
            case "kill":
                parseKill(event, matchData, eventos);
                break;
            case "bomb_planted":
                parseBombPlanted(event, matchData, eventos);
                break;
            case "bomb_defused":
                parseBombDefused(event, matchData, eventos);
                break;
            case "bomb_exploded":
                parseBombExploded(event, matchData, eventos);
                break;
            case "round_stats":
                parseRoundStats(event, matchData);
                break;
            case "match_end":
                parseMatchEnd(event, matchData, eventos);
                break;
            default:
                // Evento desconhecido, mas tenta atualizar dados comuns
                updateCommonData(event, matchData);
                break;
        }
        
        // Atualiza dados comuns que podem estar em qualquer evento
        updateCommonData(event, matchData);
        
        // Se houver match_id, tenta buscar e associar partida do sistema
        // Se não encontrar, apenas continua exibindo eventos do MatchZy normalmente
        if (event.getMatch_id() != null && !event.getMatch_id().trim().isEmpty()) {
            try {
                matchZyPartidaService.associarPartidaSistema(matchData, event.getMatch_id());
                
                // Alinha players do MatchZy com players do sistema (se partida foi encontrada)
                // Se não encontrar partida, este método simplesmente retorna sem fazer nada
                matchZyPartidaService.alinharPlayersComSistema(matchData);
            } catch (Exception e) {
                // Em caso de erro, apenas loga e continua processando eventos do MatchZy
                System.out.println("⚠️ Erro ao processar associação de partida (continuando com MatchZy): " + e.getMessage());
                // Não interrompe o processamento - continua exibindo eventos normalmente
            }
        }
        
        // Mantém apenas os últimos N eventos
        while (eventos.size() > MAX_EVENTOS) {
            eventos.remove(0);
        }
    }
    
    /**
     * Processa evento match_start
     */
    private void parseMatchStart(MatchZyEventDTO event, MatchData matchData, List<String> eventos) {
        if (event.getMap_name() != null) {
            matchData.setMapa(event.getMap_name());
        }
        
        if (event.getScore_ct() != null) {
            matchData.setScoreCT(event.getScore_ct());
        }
        if (event.getScore_t() != null) {
            matchData.setScoreT(event.getScore_t());
        }
        
        matchData.setFase("warmup");
        eventos.add("🏁 Partida iniciada - Mapa: " + (event.getMap_name() != null ? event.getMap_name() : "N/A"));
    }
    
    /**
     * Processa evento round_start
     */
    private void parseRoundStart(MatchZyEventDTO event, MatchData matchData, List<String> eventos) {
        matchData.setFase("freezetime");
        matchData.setTimestampRoundInicio(System.currentTimeMillis());
        matchData.setTempoRound(20); // Freeze time
        
        if (event.getRound_number() != null) {
            matchData.setRoundAtual(event.getRound_number());
        }
        
        // Limpa estado da bomba
        matchData.setEstadoBomba("none");
        matchData.setTimestampBombaPlantada(0);
        
        eventos.add("⏱️ Round " + (event.getRound_number() != null ? event.getRound_number() : "?") + " iniciado - Freeze time");
    }
    
    /**
     * Processa evento round_freeze_end (fim do freeze time, round começa)
     */
    private void parseRoundFreezeEnd(MatchZyEventDTO event, MatchData matchData, List<String> eventos) {
        matchData.setFase("live");
        matchData.setTimestampRoundInicio(System.currentTimeMillis());
        matchData.setTempoRound(120); // 2 minutos de round
        
        eventos.add("⚡ Freeze time acabou - Round em andamento");
    }
    
    /**
     * Processa evento going_live (partida está em andamento)
     */
    private void parseGoingLive(MatchZyEventDTO event, MatchData matchData, List<String> eventos) {
        matchData.setFase("live");
        eventos.add("🚀 Partida em andamento!");
    }
    
    /**
     * Processa evento round_end
     * O MatchZy envia estatísticas dos players neste evento
     * Pode vir em dois formatos:
     * 1. Formato padrão com players no data
     * 2. Formato com team1/team2 contendo players com stats detalhadas (dados no nível raiz ou no data)
     */
    private void parseRoundEnd(MatchZyEventDTO event, MatchData matchData, List<String> eventos) {
        matchData.setFase("over");
        matchData.setEstadoBomba("none");
        matchData.setTimestampBombaPlantada(0);
        
        // Atualiza round_number se disponível
        if (event.getRound_number() != null) {
            matchData.setRoundAtual(event.getRound_number());
        }
        
        // Processa estatísticas dos players do round_end
        Map<String, Object> data = event.getData();
        
        // Verifica se é o novo formato com team1/team2 (pode estar no data ou no nível raiz)
        boolean hasTeamFormat = (data != null && (data.containsKey("team1") || data.containsKey("team2")));
        
        if (hasTeamFormat) {
            // Formato novo com team1/team2
            parseRoundEndWithTeams(data, matchData, event);
        } else {
            // Formato padrão
            if (data != null) {
                parsePlayersFromRoundEnd(data, matchData);
            }
        }
        
        // Atualiza scores apenas se não foram atualizados pelo formato team1/team2
        // (os scores dos times têm prioridade)
        if (!hasTeamFormat) {
            if (event.getScore_ct() != null) {
                matchData.setScoreCT(event.getScore_ct());
            }
            if (event.getScore_t() != null) {
                matchData.setScoreT(event.getScore_t());
            }
        }
        
        // Extrai time vencedor do evento se disponível
        String winner = extractWinner(event);
        String eventoMsg = "🏁 Round " + (matchData.getRoundAtual() != 0 ? matchData.getRoundAtual() : "?") + " finalizado";
        if (winner != null) {
            eventoMsg += " - Vencedor: " + winner;
        }
        eventoMsg += " | Placar: " + (matchData.getNomeTimeCT() != null ? matchData.getNomeTimeCT() : "CT") + 
                     " " + matchData.getScoreCT() + " x " + matchData.getScoreT() + 
                     " " + (matchData.getNomeTimeT() != null ? matchData.getNomeTimeT() : "T");
        
        eventos.add(eventoMsg);
    }
    
    /**
     * Processa round_end com formato team1/team2
     * Formato: {"reason":1,"winner":{"side":"2","team":"team2"},"team1":{...},"team2":{...}}
     */
    private void parseRoundEndWithTeams(Map<String, Object> data, MatchData matchData, MatchZyEventDTO event) {
        try {
            // Atualiza round_number se disponível no evento
            if (event.getRound_number() != null) {
                matchData.setRoundAtual(event.getRound_number());
            }
            
            // Primeiro, verifica se existe match_id e busca partida do sistema
            String matchId = event.getMatch_id();
            if (matchId != null && !matchId.trim().isEmpty()) {
                matchZyPartidaService.associarPartidaSistema(matchData, matchId);
            }
            
            List<br.com.champ.vo.PlayerInfo> playersCT = new ArrayList<>();
            List<br.com.champ.vo.PlayerInfo> playersT = new ArrayList<>();
            
            // Processa team1 (geralmente CT)
            Object team1Obj = data.get("team1");
            if (team1Obj instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> team1 = (Map<String, Object>) team1Obj;
                processTeamPlayers(team1, playersCT, 2, matchData); // 2 = CT
                
                    // Atualiza nome do time (importante fazer antes de processar players)
                String teamName = getStringFromData(team1, "name");
                if (teamName != null && !teamName.trim().isEmpty()) {
                    matchData.setNomeTimeCT(teamName);
                }
                
                // Atualiza scores se disponíveis
                Integer score = getIntegerFromData(team1, "score");
                if (score != null) {
                    matchData.setScoreCT(score);
                }
            }
            
            // Processa team2 (geralmente T)
            Object team2Obj = data.get("team2");
            if (team2Obj instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> team2 = (Map<String, Object>) team2Obj;
                
                // Atualiza nome do time (importante fazer antes de processar players)
                String teamName = getStringFromData(team2, "name");
                if (teamName != null && !teamName.trim().isEmpty()) {
                    matchData.setNomeTimeT(teamName);
                }
                
                processTeamPlayers(team2, playersT, 3, matchData); // 3 = T
                
                // Atualiza scores se disponíveis
                Integer score = getIntegerFromData(team2, "score");
                if (score != null) {
                    matchData.setScoreT(score);
                }
            }
            
            // Processa winner - não incrementa score pois já vem atualizado no team1/team2.score
            // Os scores já estão atualizados nos times acima
            
            // Atualiza listas de players (sempre atualiza, mesmo se vazio para limpar dados antigos)
            matchData.setPlayersCT(playersCT);
            matchData.setPlayersT(playersT);
            
            System.out.println("✅ Round_end processado - CT: " + playersCT.size() + " players, T: " + playersT.size() + " players");
            System.out.println("   Nome CT: " + (matchData.getNomeTimeCT() != null ? matchData.getNomeTimeCT() : "N/A"));
            System.out.println("   Nome T: " + (matchData.getNomeTimeT() != null ? matchData.getNomeTimeT() : "N/A"));
            System.out.println("   Score CT: " + matchData.getScoreCT() + " x T: " + matchData.getScoreT());
            
            // Alinha players e times com sistema se partida foi encontrada
            if (matchData.getPartidaId() != null) {
                matchZyPartidaService.alinharPlayersComSistema(matchData);
                matchZyPartidaService.alinharNomesTimesComSistema(matchData);
                System.out.println("   Partida do sistema associada - nomes alinhados");
            } else {
                System.out.println("   Partida não encontrada no sistema - usando dados do MatchZy");
            }
            
        } catch (Exception e) {
            System.out.println("❌ Erro ao processar round_end com teams: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Processa players de um team (team1 ou team2)
     */
    private void processTeamPlayers(Map<String, Object> teamData, List<br.com.champ.vo.PlayerInfo> playersList, 
                                     int teamNumber, MatchData matchData) {
        try {
            Object playersObj = teamData.get("players");
            if (playersObj == null || !(playersObj instanceof List)) {
                return;
            }
            
            @SuppressWarnings("unchecked")
            List<Object> players = (List<Object>) playersObj;
            
            for (Object playerObj : players) {
                if (playerObj instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> playerData = (Map<String, Object>) playerObj;
                    
                    br.com.champ.vo.PlayerInfo player = createPlayerInfoFromTeamFormat(playerData, teamNumber);
                    if (player != null) {
                        playersList.add(player);
                    }
                }
            }
            
        } catch (Exception e) {
            System.out.println("Erro ao processar players do team: " + e.getMessage());
        }
    }
    
    /**
     * Cria PlayerInfo a partir do formato team1/team2 (com stats dentro)
     */
    private br.com.champ.vo.PlayerInfo createPlayerInfoFromTeamFormat(Map<String, Object> playerData, int teamNumber) {
        br.com.champ.vo.PlayerInfo player = new br.com.champ.vo.PlayerInfo();
        
        try {
            // Nome
            String name = getStringFromData(playerData, "name");
            if (name != null) {
                player.setNome(name);
            }
            
            // Steam ID
            String steamId = getStringFromData(playerData, "steamid");
            if (steamId != null) {
                player.setSteamId(steamId);
            }
            
            // Time
            player.setTime(teamNumber);
            
            // Stats (estatísticas dentro de um objeto "stats")
            Object statsObj = playerData.get("stats");
            if (statsObj instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> stats = (Map<String, Object>) statsObj;
                
                // Kills
                Integer kills = getIntegerFromData(stats, "kills");
                if (kills != null) {
                    player.setKills(kills);
                }
                
                // Deaths
                Integer deaths = getIntegerFromData(stats, "deaths");
                if (deaths != null) {
                    player.setDeaths(deaths);
                }
                
                // Assists
                Integer assists = getIntegerFromData(stats, "assists");
                if (assists != null) {
                    player.setAssists(assists);
                }
                
                // Damage
                Integer damage = getIntegerFromData(stats, "damage");
                if (damage != null) {
                    player.setDamage(damage);
                }
                
                // Health (opcional)
                Integer health = getIntegerFromData(stats, "health");
                if (health != null) {
                    player.setHealth(health);
                    player.setAlive(health > 0);
                } else {
                    player.setAlive(true);
                    player.setHealth(100);
                }
            }
            
        } catch (Exception e) {
            System.out.println("Erro ao criar PlayerInfo do formato team: " + e.getMessage());
            return null;
        }
        
        return player;
    }
    
    /**
     * Processa evento kill
     */
    private void parseKill(MatchZyEventDTO event, MatchData matchData, List<String> eventos) {
        Map<String, Object> data = event.getData();
        if (data == null) {
            return;
        }
        
        String killer = getStringFromData(data, "killer_name", "killer");
        String victim = getStringFromData(data, "victim_name", "victim");
        String weapon = getStringFromData(data, "weapon");
        Boolean headshot = getBooleanFromData(data, "headshot");
        
        if (killer != null && victim != null) {
            String evento = "⚔️ " + killer + " matou " + victim;
            if (weapon != null) {
                evento += " com " + weapon;
            }
            if (headshot != null && headshot) {
                evento += " (💀 HEADSHOT)";
            }
            
            eventos.add(evento);
            
            // Atualiza informações da última kill
            matchData.setUltimaKillKiller(killer);
            matchData.setUltimaKillVictim(victim);
            matchData.setUltimaKillWeapon(weapon != null ? weapon : "unknown");
            matchData.setUltimaKillHeadshot(headshot != null && headshot);
            matchData.setTimestampUltimaKill(System.currentTimeMillis());
        }
    }
    
    /**
     * Processa evento bomb_planted
     */
    private void parseBombPlanted(MatchZyEventDTO event, MatchData matchData, List<String> eventos) {
        Map<String, Object> data = event.getData();
        if (data == null) {
            return;
        }
        
        String planter = getStringFromData(data, "planter_name", "planter");
        String site = getStringFromData(data, "bombsite", "site");
        
        matchData.setEstadoBomba("planted");
        matchData.setTimestampBombaPlantada(System.currentTimeMillis());
        matchData.setTempoBombaRestante(40);
        
        if (planter != null) {
            matchData.setQuemPlantouBomba(planter);
        }
        if (site != null) {
            matchData.setBombsite(site.toUpperCase());
        }
        
        String evento = "💣 Bomba plantada";
        if (planter != null) {
            evento += " por " + planter;
        }
        if (site != null) {
            evento += " no bombsite " + site.toUpperCase();
        }
        
        eventos.add(evento);
    }
    
    /**
     * Processa evento bomb_defused
     */
    private void parseBombDefused(MatchZyEventDTO event, MatchData matchData, List<String> eventos) {
        Map<String, Object> data = event.getData();
        
        String defuser = null;
        if (data != null) {
            defuser = getStringFromData(data, "defuser_name", "defuser");
        }
        
        matchData.setEstadoBomba("defused");
        matchData.setTimestampBombaPlantada(0);
        matchData.setTempoBombaRestante(0);
        
        String evento = "✅ Bomba desarmada";
        if (defuser != null) {
            evento += " por " + defuser;
        }
        
        eventos.add(evento);
    }
    
    /**
     * Processa evento bomb_exploded
     */
    private void parseBombExploded(MatchZyEventDTO event, MatchData matchData, List<String> eventos) {
        matchData.setEstadoBomba("exploded");
        matchData.setTimestampBombaPlantada(0);
        matchData.setTempoBombaRestante(0);
        
        eventos.add("💥 Bomba explodiu!");
    }
    
    /**
     * Processa evento round_stats (estatísticas do round)
     */
    private void parseRoundStats(MatchZyEventDTO event, MatchData matchData) {
        // Atualiza scores e outros dados do round
        if (event.getScore_ct() != null) {
            matchData.setScoreCT(event.getScore_ct());
        }
        if (event.getScore_t() != null) {
            matchData.setScoreT(event.getScore_t());
        }
        if (event.getRound_number() != null) {
            matchData.setRoundAtual(event.getRound_number());
        }
        
        // Tenta extrair informações de players do data
        Map<String, Object> data = event.getData();
        if (data != null) {
            // Se houver dados de players no evento, processa
            parsePlayersFromData(data, matchData);
        }
    }
    
    /**
     * Processa evento match_end
     */
    private void parseMatchEnd(MatchZyEventDTO event, MatchData matchData, List<String> eventos) {
        matchData.setFase("over");
        
        if (event.getScore_ct() != null && event.getScore_t() != null) {
            matchData.setScoreCT(event.getScore_ct());
            matchData.setScoreT(event.getScore_t());
            
            String winner = extractWinner(event);
            String eventoMsg = "🏆 Partida finalizada";
            if (winner != null) {
                eventoMsg += " - Vencedor: " + winner;
            }
            eventoMsg += " | Placar Final: CT " + event.getScore_ct() + " x " + event.getScore_t() + " T";
            
            eventos.add(eventoMsg);
        }
    }
    
    /**
     * Atualiza dados comuns que podem estar em qualquer evento
     */
    private void updateCommonData(MatchZyEventDTO event, MatchData matchData) {
        if (event.getMap_name() != null && !event.getMap_name().isEmpty()) {
            matchData.setMapa(event.getMap_name());
        }
        
        if (event.getScore_ct() != null) {
            matchData.setScoreCT(event.getScore_ct());
        }
        
        if (event.getScore_t() != null) {
            matchData.setScoreT(event.getScore_t());
        }
        
        if (event.getRound_number() != null) {
            matchData.setRoundAtual(event.getRound_number());
        }
        
        matchData.setConectado(true);
    }
    
    /**
     * Extrai o time vencedor do evento
     */
    private String extractWinner(MatchZyEventDTO event) {
        Map<String, Object> data = event.getData();
        if (data == null) {
            return null;
        }
        
        String winner = getStringFromData(data, "winner", "winning_team");
        if (winner != null) {
            // Normaliza para CT ou T
            if (winner.equalsIgnoreCase("ct") || winner.equalsIgnoreCase("counter-terrorist")) {
                return "CT";
            } else if (winner.equalsIgnoreCase("t") || winner.equalsIgnoreCase("terrorist")) {
                return "T";
            }
            return winner.toUpperCase();
        }
        
        return null;
    }
    
    /**
     * Extrai string dos dados do evento (tenta várias chaves)
     */
    private String getStringFromData(Map<String, Object> data, String... keys) {
        if (data == null) {
            return null;
        }
        
        for (String key : keys) {
            Object value = data.get(key);
            if (value != null) {
                return value.toString();
            }
        }
        
        return null;
    }
    
    /**
     * Extrai boolean dos dados do evento
     */
    private Boolean getBooleanFromData(Map<String, Object> data, String key) {
        if (data == null) {
            return null;
        }
        
        Object value = data.get(key);
        if (value == null) {
            return null;
        }
        
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        
        if (value instanceof String) {
            return Boolean.parseBoolean((String) value);
        }
        
        return null;
    }
    
    /**
     * Processa informações de players do evento round_end
     * O MatchZy envia estatísticas completas dos players neste evento
     */
    private void parsePlayersFromRoundEnd(Map<String, Object> data, MatchData matchData) {
        try {
            // O MatchZy pode enviar players em diferentes formatos
            // Formato comum: "players" -> array/object com dados dos players
            
            Object playersObj = data.get("players");
            if (playersObj == null) {
                return;
            }
            
            List<br.com.champ.vo.PlayerInfo> playersCT = new ArrayList<>();
            List<br.com.champ.vo.PlayerInfo> playersT = new ArrayList<>();
            
            // Se for um Map (object JSON)
            if (playersObj instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> playersMap = (Map<String, Object>) playersObj;
                
                for (Map.Entry<String, Object> entry : playersMap.entrySet()) {
                    Object playerDataObj = entry.getValue();
                    if (playerDataObj instanceof Map) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> playerData = (Map<String, Object>) playerDataObj;
                        
                        br.com.champ.vo.PlayerInfo player = createPlayerInfoFromData(playerData);
                        if (player != null) {
                            if (player.getTime() == 2) {
                                playersCT.add(player);
                            } else if (player.getTime() == 3) {
                                playersT.add(player);
                            }
                        }
                    }
                }
            }
            // Se for uma List (array JSON)
            else if (playersObj instanceof List) {
                @SuppressWarnings("unchecked")
                List<Object> playersList = (List<Object>) playersObj;
                
                for (Object playerObj : playersList) {
                    if (playerObj instanceof Map) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> playerData = (Map<String, Object>) playerObj;
                        
                        br.com.champ.vo.PlayerInfo player = createPlayerInfoFromData(playerData);
                        if (player != null) {
                            if (player.getTime() == 2) {
                                playersCT.add(player);
                            } else if (player.getTime() == 3) {
                                playersT.add(player);
                            }
                        }
                    }
                }
            }
            
            // Atualiza listas de players no MatchData
            if (!playersCT.isEmpty()) {
                matchData.setPlayersCT(playersCT);
            }
            if (!playersT.isEmpty()) {
                matchData.setPlayersT(playersT);
            }
            
        } catch (Exception e) {
            System.out.println("Erro ao processar players do round_end: " + e.getMessage());
            // Não interrompe o processamento - continua normalmente
        }
    }
    
    /**
     * Cria um PlayerInfo a partir dos dados do evento MatchZy
     */
    private br.com.champ.vo.PlayerInfo createPlayerInfoFromData(Map<String, Object> playerData) {
        br.com.champ.vo.PlayerInfo player = new br.com.champ.vo.PlayerInfo();
        
        try {
            // Nome
            String name = getStringFromData(playerData, "name", "player_name", "username");
            if (name != null) {
                player.setNome(name);
            }
            
            // Steam ID
            String steamId = getStringFromData(playerData, "steam_id", "steamid", "steamId", "steamId64");
            if (steamId != null) {
                player.setSteamId(steamId);
            }
            
            // Time (CT = 2, T = 3)
            String team = getStringFromData(playerData, "team", "team_name");
            if (team != null) {
                if (team.equalsIgnoreCase("CT") || team.equalsIgnoreCase("counter-terrorist") || team.equalsIgnoreCase("counter_terrorist")) {
                    player.setTime(2);
                } else if (team.equalsIgnoreCase("T") || team.equalsIgnoreCase("terrorist")) {
                    player.setTime(3);
                }
            } else {
                // Tenta por número
                Integer teamNum = getIntegerFromData(playerData, "team_number", "team");
                if (teamNum != null) {
                    player.setTime(teamNum);
                }
            }
            
            // Kills
            Integer kills = getIntegerFromData(playerData, "kills", "k", "kill_count");
            if (kills != null) {
                player.setKills(kills);
            }
            
            // Deaths
            Integer deaths = getIntegerFromData(playerData, "deaths", "d", "death_count");
            if (deaths != null) {
                player.setDeaths(deaths);
            }
            
            // Assists
            Integer assists = getIntegerFromData(playerData, "assists", "a", "assist_count");
            if (assists != null) {
                player.setAssists(assists);
            }
            
            // Damage
            Integer damage = getIntegerFromData(playerData, "damage", "dmg", "total_damage", "damage_dealt");
            if (damage != null) {
                player.setDamage(damage);
            }
            
            // Health (opcional)
            Integer health = getIntegerFromData(playerData, "health", "hp");
            if (health != null) {
                player.setHealth(health);
                player.setAlive(health > 0);
            } else {
                // Assume vivo por padrão se não tiver informação
                player.setAlive(true);
                player.setHealth(100);
            }
            
            // Armor (opcional)
            Integer armor = getIntegerFromData(playerData, "armor", "armor_value");
            if (armor != null) {
                player.setArmor(armor);
            }
            
        } catch (Exception e) {
            System.out.println("Erro ao criar PlayerInfo: " + e.getMessage());
            return null;
        }
        
        return player;
    }
    
    /**
     * Extrai integer dos dados do evento (tenta várias chaves)
     */
    private Integer getIntegerFromData(Map<String, Object> data, String... keys) {
        if (data == null) {
            return null;
        }
        
        for (String key : keys) {
            Object value = data.get(key);
            if (value != null) {
                if (value instanceof Integer) {
                    return (Integer) value;
                }
                if (value instanceof Number) {
                    return ((Number) value).intValue();
                }
                if (value instanceof String) {
                    try {
                        return Integer.parseInt((String) value);
                    } catch (NumberFormatException e) {
                        // Tenta próximo
                    }
                }
            }
        }
        
        return null;
    }
    
    /**
     * Processa informações de players do evento (método genérico)
     */
    private void parsePlayersFromData(Map<String, Object> data, MatchData matchData) {
        // Este método pode ser usado por outros eventos que também enviem dados de players
        parsePlayersFromRoundEnd(data, matchData);
    }
}

