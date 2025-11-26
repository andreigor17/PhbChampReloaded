/*
 * Resource JAX-RS para receber dados do Game State Integration (GSI) do CS2
 */
package br.com.irontech.phbchampreloaded.resources;

import br.com.champ.Servico.CustomRoundStatsParser;
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
 * Endpoint para receber dados do GSI do Counter-Strike 2 O jogo envia dados via
 * HTTP POST neste endpoint
 */
@Path("/gsi")
public class GSIResource {

    @EJB
    private GameStateService gameStateService;

    @EJB
    private GameStateParser gameStateParser;
    
    @EJB
    private CustomRoundStatsParser customRoundStatsParser;

    /**
     * Recebe dados do GSI via POST Este endpoint será chamado pelo CS2 sempre
     * que houver mudanças no estado do jogo
     */
    @POST
    @Consumes({MediaType.APPLICATION_JSON, MediaType.WILDCARD})
    @Produces(MediaType.TEXT_PLAIN)
    public Response receiveGameState(String payload, @Context HttpServletRequest request) {
        try {
            String jsonString = payload;
            System.err.println(jsonString);
            
            // Se o payload vier vazio, tenta ler do request
            if (jsonString == null || jsonString.trim().isEmpty()) {
                try (BufferedReader reader = request.getReader()) {
                    jsonString = reader.lines().collect(Collectors.joining("\n"));
                }
            }
            
            // Guarda o payload original para tentar parsear depois
            String originalPayload = jsonString;
            
            // Tenta extrair JSON do meio de logs se necessário
            jsonString = extractJsonFromLogs(jsonString);
            
            // Se não conseguiu extrair, usa o payload original
            if (jsonString == null || jsonString.trim().isEmpty()) {
                jsonString = originalPayload;
            }
            
            if (jsonString == null || jsonString.trim().isEmpty()) {
                // Se ainda estiver vazio, retorna OK mas não processa
                return Response.ok("OK").build();
            }
            
            // Log para debug
            System.out.println("=== GSI Data Received ===");
            System.out.println("Payload length: " + jsonString.length());
            
            MatchData matchData = null;
            
            // Valida se é um JSON válido antes de tentar parsear
            String trimmed = jsonString.trim();
            boolean isValidJson = trimmed.startsWith("{") && trimmed.endsWith("}");
            
            // Tenta parsear como GSI padrão primeiro (apenas se for JSON válido)
            if (isValidJson) {
                try {
                    matchData = gameStateParser.parseGSIJson(jsonString);
                    if (matchData != null && matchData.getMapa() != null && !matchData.getMapa().isEmpty()) {
                        System.out.println("Parsed as standard GSI format");
                    }
                } catch (Exception e) {
                    System.out.println("Failed to parse as standard GSI: " + e.getMessage());
                    // Continua para tentar formato customizado
                    matchData = null;
                }
            } else {
                System.out.println("Payload não é JSON válido (não começa com {), tentando formato customizado");
            }
            
            // Se não funcionou ou veio vazio, tenta formato customizado round_stats
            // Usa o payload original que pode ter logs misturados
            if (matchData == null || matchData.getMapa() == null || matchData.getMapa().isEmpty()) {
                try {
                    MatchData customData = customRoundStatsParser.parseRoundStats(originalPayload);
                    if (customData != null && (customData.getMapa() != null || customData.getScoreCT() > 0 || customData.getScoreT() > 0)) {
                        matchData = customData;
                        System.out.println("Parsed as custom round_stats format");
                    }
                } catch (Exception e) {
                    System.out.println("Failed to parse as custom format: " + e.getMessage());
                    // Não imprime stack trace completo para não poluir logs
                }
            }
            
            // Se ainda não conseguiu, cria um objeto vazio mas marca como conectado
            if (matchData == null) {
                matchData = new MatchData();
                matchData.setConectado(true);
            }
            
            // Atualiza o serviço com os novos dados
            gameStateService.updateGameState(matchData);
            
            System.out.println("=== Data Processed - Map: " + matchData.getMapa() + 
                             ", CT: " + matchData.getScoreCT() + 
                             ", T: " + matchData.getScoreT() + " ===");

            // Retorna 200 OK - o jogo espera uma resposta 2XX
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
     * Endpoint de teste - também processa dados como o endpoint principal
     * Mantido para compatibilidade
     */
    @POST
    @Path("/test")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.WILDCARD})
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
        return Response.ok("GSI Service is running (GET)").build();
    }

    /**
     * Endpoint raiz GET para verificar se o recurso está acessível
     */
    @jakarta.ws.rs.GET
    @Produces(MediaType.TEXT_PLAIN)
    public Response root() {
        return Response.ok("GSI Resource is accessible. Use POST /resources/gsi to send game state data.").build();
    }
}
