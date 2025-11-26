/*
 * Resource JAX-RS para receber dados do Game State Integration (GSI) do CS2
 */
package br.com.irontech.phbchampreloaded.resources;

import br.com.champ.Servico.CustomRoundStatsParser;
import br.com.champ.Servico.EventParser;
import br.com.champ.Servico.GameStateParser;
import br.com.champ.Servico.GameStateService;
import br.com.champ.vo.MatchData;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ejb.EJB;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.io.BufferedReader;
import java.util.stream.Collectors;

/**
 * Endpoint para receber logs do Counter-Strike 2 via logaddress_add_http
 * O servidor CS2 envia logs via HTTP POST neste endpoint
 * 
 * Comando no servidor CS2:
 * logaddress_add_http http://seu-servidor:porta/PhbChampReloaded/resources/logs
 */
@Path("/gsi")
public class GSIResource {

    @EJB
    private GameStateService gameStateService;

    @EJB
    private GameStateParser gameStateParser;
    
    @EJB
    private CustomRoundStatsParser customRoundStatsParser;
    
    @EJB
    private EventParser eventParser;

    /**
     * Recebe logs do servidor CS2 via logaddress_add_http
     * Este endpoint será chamado pelo servidor CS2 sempre que houver eventos
     * Os logs vêm como texto puro do console do servidor
     */
    @POST
    @Consumes({MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON, MediaType.WILDCARD})
    @Produces(MediaType.TEXT_PLAIN)
    public Response receiveGameState(String payload, @Context HttpServletRequest request) {
        try {
            // Logs do logaddress_add_http vêm como texto puro
            String logContent = payload;
            
            // Debug: mostra o que recebeu
            if (logContent != null && !logContent.trim().isEmpty()) {
                System.out.println("=== CS2 LOG RECEIVED ===");
                System.out.println(logContent.substring(0, Math.min(500, logContent.length())));
            }
            
            // Se o payload vier vazio, tenta ler do request
            if (logContent == null || logContent.trim().isEmpty()) {
                try (BufferedReader reader = request.getReader()) {
                    logContent = reader.lines().collect(Collectors.joining("\n"));
                }
            }
            
            // Guarda o payload original (logs brutos)
            String originalPayload = logContent;
            
            if (logContent == null || logContent.trim().isEmpty()) {
                // Se ainda estiver vazio, retorna OK mas não processa
                return Response.ok("OK").build();
            }
            
            // Obtém o estado atual do jogo (ou cria um novo)
            MatchData matchData = gameStateService.getCurrentGameState();
            if (matchData == null) {
                matchData = new MatchData();
            }
            
            // Para logaddress_add_http, processamos apenas os eventos dos logs
            // Primeiro tenta extrair JSON se houver (formato customizado round_stats)
            String jsonString = extractJsonFromLogs(logContent);
            
            if (jsonString != null && !jsonString.trim().isEmpty()) {
                // Se encontrou JSON, tenta parsear dados estruturados
                try {
                    MatchData parsedData = customRoundStatsParser.parseRoundStats(originalPayload);
                    if (parsedData != null && parsedData.getMapa() != null) {
                        // Atualiza dados principais
                        matchData.setMapa(parsedData.getMapa());
                        matchData.setScoreCT(parsedData.getScoreCT());
                        matchData.setScoreT(parsedData.getScoreT());
                        matchData.setRoundAtual(parsedData.getRoundAtual());
                        if (parsedData.getPlayersCT() != null) {
                            matchData.setPlayersCT(parsedData.getPlayersCT());
                        }
                        if (parsedData.getPlayersT() != null) {
                            matchData.setPlayersT(parsedData.getPlayersT());
                        }
                        System.out.println("Parsed JSON data from logs");
                    }
                } catch (Exception e) {
                    System.out.println("Failed to parse JSON from logs: " + e.getMessage());
                }
            }
            
            // Parse de eventos dos logs (kills, bomb plant, round start, etc)
            try {
                eventParser.parseEvents(originalPayload, matchData);
            } catch (Exception e) {
                System.out.println("Erro ao parsear eventos dos logs: " + e.getMessage());
            }
            
            // Atualiza timers baseados nos timestamps
            matchData.calcularTempoBombaRestante();
            matchData.calcularTempoRoundRestante();
            
            // Marca como conectado
            matchData.setConectado(true);
            
            // Atualiza o serviço com os novos dados
            gameStateService.updateGameState(matchData);
            
            System.out.println("=== Log Processed - Map: " + (matchData.getMapa() != null ? matchData.getMapa() : "N/A") + 
                             ", CT: " + matchData.getScoreCT() + 
                             ", T: " + matchData.getScoreT() + " ===");

            // Retorna 200 OK - o servidor CS2 espera uma resposta 2XX
            return Response.ok("OK").build();

        } catch (Exception e) {
            e.printStackTrace();
            // Retorna OK mesmo com erro para não interromper o GSI
            // O jogo espera resposta 2XX
            return Response.ok("OK").build();
        }
    }
    
    /**
     * Extrai JSON do meio de logs do CS2
     * O CS2 às vezes envia logs misturados com JSON
     */
    private String extractJsonFromLogs(String content) {
        if (content == null || content.trim().isEmpty()) {
            return null;
        }
        
        // Primeiro, tenta parsear direto como JSON
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.readTree(content);
            return content; // Se funcionou, retorna como está
        } catch (Exception e) {
            // Não é JSON puro, tenta extrair
        }
        
        // Procura por JSON_BEGIN ... JSON_END (formato customizado do CS2)
        int beginIndex = content.indexOf("JSON_BEGIN{");
        int endIndex = content.indexOf("}JSON_END");
        
        if (beginIndex >= 0 && endIndex > beginIndex) {
            String jsonPart = content.substring(beginIndex + "JSON_BEGIN{".length(), endIndex + 1);
            
            // Remove timestamps do início das linhas (formato: 11/26/2025 - 02:12:21.266 - )
            jsonPart = jsonPart.replaceAll("(?m)^\\d{2}/\\d{2}/\\d{4} - \\d{2}:\\d{2}:\\d{2}\\.\\d{3} - ", "");
            
            // Limpa espaços extras
            jsonPart = jsonPart.trim();
            
            // Valida se começa com { e tenta parsear
            jsonPart = jsonPart.trim();
            if (jsonPart.startsWith("{") && jsonPart.endsWith("}")) {
                try {
                    ObjectMapper mapper = new ObjectMapper();
                    mapper.readTree(jsonPart);
                    return jsonPart;
                } catch (Exception e) {
                    System.out.println("JSON extraído não é válido: " + jsonPart.substring(0, Math.min(200, jsonPart.length())));
                }
            }
        }
        
        // Se não encontrou JSON_BEGIN, procura por { ... } que seja JSON válido
        int firstBrace = content.indexOf('{');
        int lastBrace = content.lastIndexOf('}');
        
        if (firstBrace >= 0 && lastBrace > firstBrace) {
            String candidate = content.substring(firstBrace, lastBrace + 1);
            
            // Remove timestamps se houver
            candidate = candidate.replaceAll("(?m)^\\d{2}/\\d{2}/\\d{4} - \\d{2}:\\d{2}:\\d{2}\\.\\d{3} - ", "");
            candidate = candidate.trim();
            
            // Valida se começa com { antes de tentar parsear
            if (candidate.startsWith("{") && candidate.endsWith("}")) {
                try {
                    ObjectMapper mapper = new ObjectMapper();
                    mapper.readTree(candidate);
                    return candidate;
                } catch (Exception e) {
                    // Não é JSON válido
                }
            }
        }
        
        return null;
    }

    /**
     * Endpoint de teste - também processa logs como o endpoint principal
     * Mantido para compatibilidade
     */
    @POST
    @Path("/test")
    @Consumes({MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON, MediaType.WILDCARD})
    @Produces(MediaType.TEXT_PLAIN)
    public Response testPOST(String payload, @Context HttpServletRequest request) {
        // Redireciona para o método principal de processamento
        return receiveGameState(payload, request);
    }

    /**
     * Endpoint GET para testar se o serviço está funcionando
     */
    @jakarta.ws.rs.GET
    @Path("/test")
    @Produces(MediaType.TEXT_PLAIN)
    public Response testGET() {
        return Response.ok("CS2 Logs Service is running (GET). Use POST to send logs.").build();
    }

    /**
     * Endpoint raiz GET para verificar se o recurso está acessível
     */
    @jakarta.ws.rs.GET
    @Produces(MediaType.TEXT_PLAIN)
    public Response root() {
        return Response.ok("CS2 Logs Resource is accessible. Use POST /resources/logs to send server logs.\n" +
                          "Configure no servidor CS2: logaddress_add_http http://seu-servidor:porta/PhbChampReloaded/resources/logs").build();
    }
}
