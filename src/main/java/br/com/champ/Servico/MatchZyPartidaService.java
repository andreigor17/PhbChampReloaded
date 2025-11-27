/*
 * Serviço para buscar e associar partida do sistema com eventos do MatchZy
 */
package br.com.champ.Servico;

import br.com.champ.Modelo.Partida;
import br.com.champ.Modelo.Player;
import br.com.champ.vo.MatchData;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import java.util.HashMap;
import java.util.Map;

/**
 * Serviço para buscar partida do sistema e alinhar com dados do MatchZy
 */
@Stateless
public class MatchZyPartidaService {
    
    @EJB
    private PartidaServico partidaServico;
    
    @EJB
    private PlayerServico playerServico;
    
    /**
     * Busca partida do sistema pelo match_id do MatchZy e associa ao MatchData
     * Se não encontrar a partida, apenas continua exibindo eventos do MatchZy normalmente
     */
    public void associarPartidaSistema(MatchData matchData, String matchZyMatchId) {
        if (matchZyMatchId == null || matchZyMatchId.trim().isEmpty()) {
            return;
        }
        
        // Armazena o match_id do MatchZy (mesmo se não encontrar partida no sistema)
        matchData.setMatchZyMatchId(matchZyMatchId);
        
        try {
            // Tenta buscar a partida no sistema
            Partida partida = partidaServico.buscarPorMatchZyId(matchZyMatchId);
            
            if (partida != null && partida.getId() != null) {
                // Associa dados da partida ao MatchData
                matchData.setPartidaId(partida.getId());
                
                if (partida.getNome() != null && !partida.getNome().trim().isEmpty()) {
                    matchData.setPartidaNome(partida.getNome());
                }
                
                if (partida.getTimeVencedor() != null && partida.getTimeVencedor().getNome() != null) {
                    matchData.setPartidaTimeVencedor(partida.getTimeVencedor().getNome());
                }
                if (partida.getTimePerdedor() != null && partida.getTimePerdedor().getNome() != null) {
                    matchData.setPartidaTimePerdedor(partida.getTimePerdedor().getNome());
                }
                
                if (partida.getTeams() != null && !partida.getTeams().isEmpty()) {
                    matchData.setPartidaTeams(partida.getTeams());
                }
                
                if (partida.getPlayers() != null && !partida.getPlayers().isEmpty()) {
                    matchData.setPartidaPlayers(partida.getPlayers());
                    
                    // Cria mapa de players por steamId64 para facilitar busca
                    Map<String, Player> playersMap = new HashMap<>();
                    for (Player player : partida.getPlayers()) {
                        if (player != null && player.getSteamId64() != null && !player.getSteamId64().trim().isEmpty()) {
                            playersMap.put(player.getSteamId64().trim(), player);
                        }
                    }
                    if (!playersMap.isEmpty()) {
                        matchData.setPlayersPorSteamId64(playersMap);
                    }
                }
                
                System.out.println("✅ Partida do sistema associada: " + (partida.getNome() != null ? partida.getNome() : "N/A") + " (ID: " + partida.getId() + ")");
            } else {
                // Partida não encontrada - continua normalmente, apenas exibindo eventos do MatchZy
                System.out.println("ℹ️ Partida não encontrada no sistema para match_id: " + matchZyMatchId + " - Continuando apenas com eventos do MatchZy");
            }
            
        } catch (Exception e) {
            // Em caso de erro, apenas loga e continua exibindo eventos do MatchZy
            System.out.println("⚠️ Erro ao buscar partida do sistema (continuando apenas com MatchZy): " + e.getMessage());
            // Não imprime stack trace para não poluir logs em produção
            // e.printStackTrace();
        }
    }
    
    /**
     * Alinha informações de players do MatchZy com players do sistema
     * Atualiza os PlayerInfo com dados do sistema quando encontrado por steamId64
     * Se não houver players do sistema, apenas mantém os dados do MatchZy
     */
    public void alinharPlayersComSistema(MatchData matchData) {
        // Validação: se não houver mapa de players do sistema, apenas mantém dados do MatchZy
        Map<String, Player> playersMap = matchData.getPlayersPorSteamId64();
        if (playersMap == null || playersMap.isEmpty()) {
            // Sem players do sistema - mantém apenas dados do MatchZy (comportamento normal)
            return;
        }
        
        try {
            // Alinha players CT
            if (matchData.getPlayersCT() != null && !matchData.getPlayersCT().isEmpty()) {
                for (br.com.champ.vo.PlayerInfo playerInfo : matchData.getPlayersCT()) {
                    if (playerInfo != null && playerInfo.getSteamId() != null && !playerInfo.getSteamId().trim().isEmpty()) {
                        Player playerSistema = playersMap.get(playerInfo.getSteamId().trim());
                        if (playerSistema != null) {
                            // Atualiza nome do player com dados do sistema
                            if (playerSistema.getNick() != null && !playerSistema.getNick().trim().isEmpty()) {
                                playerInfo.setNome(playerSistema.getNick());
                            } else if (playerSistema.getNome() != null && !playerSistema.getNome().trim().isEmpty()) {
                                playerInfo.setNome(playerSistema.getNome());
                            }
                        }
                    }
                }
            }
            
            // Alinha players T
            if (matchData.getPlayersT() != null && !matchData.getPlayersT().isEmpty()) {
                for (br.com.champ.vo.PlayerInfo playerInfo : matchData.getPlayersT()) {
                    if (playerInfo != null && playerInfo.getSteamId() != null && !playerInfo.getSteamId().trim().isEmpty()) {
                        Player playerSistema = playersMap.get(playerInfo.getSteamId().trim());
                        if (playerSistema != null) {
                            // Atualiza nome do player com dados do sistema
                            if (playerSistema.getNick() != null && !playerSistema.getNick().trim().isEmpty()) {
                                playerInfo.setNome(playerSistema.getNick());
                            } else if (playerSistema.getNome() != null && !playerSistema.getNome().trim().isEmpty()) {
                                playerInfo.setNome(playerSistema.getNome());
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            // Em caso de erro no alinhamento, apenas loga e mantém dados do MatchZy
            System.out.println("⚠️ Erro ao alinhar players com sistema (mantendo dados do MatchZy): " + e.getMessage());
            // Não interrompe o processamento - continua com dados do MatchZy
        }
    }
}

