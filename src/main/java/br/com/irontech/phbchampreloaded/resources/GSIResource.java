/*
 * Resource JAX-RS para receber dados do Game State Integration (GSI) do CS2
 * e eventos do MatchZy
 * 
 * Suporta:
 * - Logs do CS2 via logaddress_add_http (endpoint legado)
 * - Eventos JSON do MatchZy via HTTP POST
 */
package br.com.irontech.phbchampreloaded.resources;

import br.com.champ.Servico.CustomRoundStatsParser;
import br.com.champ.Servico.EventParser;
import br.com.champ.Servico.GameStateParser;
import br.com.champ.Servico.GameStateService;
import br.com.champ.Servico.MatchZyEventParser;
import br.com.champ.vo.MatchData;
import br.com.champ.vo.MatchZyEventDTO;
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
 * Endpoint para receber dados do CS2 e eventos do MatchZy
 * 
 * ENDPOINTS:
 * - POST /gsi - Endpoint legado para logs do CS2 (logaddress_add_http)
 * - POST /gsi/matchzy - Endpoint para eventos JSON do MatchZy
 * - GET /gsi - Verifica se o serviço está acessível
 * - GET /gsi/test - Endpoint de teste
 * 
 * Configuração do MatchZy:
 * Configure a URL de eventos no MatchZy para:
 * http://seu-servidor:porta/PhbChampReloaded/resources/gsi/matchzy
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
    
    @EJB
    private MatchZyEventParser matchZyEventParser;
    
    private final ObjectMapper objectMapper = new ObjectMapper();

    // ============================================================================
    // ENDPOINT PARA EVENTOS DO MATCHZY
    // ============================================================================
    
    /**
     * Recebe eventos do MatchZy em formato JSON
     * 
     * O MatchZy envia eventos via HTTP POST no formato JSON
     * Este endpoint processa os eventos e atualiza o estado do jogo
     * 
     * Exemplo de configuração no MatchZy:
     * sm_matchzy_events_api_url "http://seu-servidor:porta/PhbChampReloaded/resources/gsi/matchzy"
     */
    @POST
    @Path("/matchzy")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response receiveMatchZyEvent(String payload, @Context HttpServletRequest request) {
        try {
            // Debug: mostra o que recebeu
            if (payload != null && !payload.trim().isEmpty()) {
                System.out.println("=== MATCHZY EVENT RECEIVED ===");
                System.out.println(payload);
            }
            
            // Se o payload vier vazio, tenta ler do request
            if (payload == null || payload.trim().isEmpty()) {
                try (BufferedReader reader = request.getReader()) {
                    payload = reader.lines().collect(Collectors.joining("\n"));
                }
            }
            
            if (payload == null || payload.trim().isEmpty()) {
                return Response.ok("{\"status\":\"ok\",\"message\":\"Empty payload\"}").build();
            }
            
            // Tenta parsear o JSON do MatchZy
            MatchZyEventDTO event;
            try {
                event = objectMapper.readValue(payload, MatchZyEventDTO.class);
            } catch (Exception e) {
                System.out.println("Erro ao parsear JSON do MatchZy: " + e.getMessage());
                System.out.println("Payload recebido: " + payload.substring(0, Math.min(500, payload.length())));
                // Retorna OK para não interromper o MatchZy, mas loga o erro
                return Response.ok("{\"status\":\"error\",\"message\":\"Invalid JSON format\"}").build();
            }
            
            if (event == null || event.getEvent() == null) {
                System.out.println("Evento do MatchZy está vazio ou sem tipo");
                return Response.ok("{\"status\":\"ok\",\"message\":\"Event processed\"}").build();
            }
            
            // Obtém o estado atual do jogo (ou cria um novo)
            MatchData matchData = gameStateService.getCurrentGameState();
            if (matchData == null) {
                matchData = new MatchData();
            }
            
            // Processa o evento do MatchZy
            // NOTA: Se não encontrar partida no sistema, continua normalmente exibindo apenas eventos do MatchZy
            try {
                matchZyEventParser.parseEvent(event, matchData);
                
                // Atualiza timers baseados nos timestamps
                matchData.calcularTempoBombaRestante();
                matchData.calcularTempoRoundRestante();
                
                // Marca como conectado
                matchData.setConectado(true);
                
                // Atualiza o serviço com os novos dados
                gameStateService.updateGameState(matchData);
                
                System.out.println("=== MatchZy Event Processed - Type: " + event.getEvent() + 
                                 ", Map: " + (matchData.getMapa() != null ? matchData.getMapa() : "N/A") + 
                                 ", CT: " + matchData.getScoreCT() + 
                                 ", T: " + matchData.getScoreT() + " ===");
                
            } catch (Exception e) {
                System.out.println("Erro ao processar evento do MatchZy: " + e.getMessage());
                e.printStackTrace();
                // Retorna OK mesmo com erro para não interromper o MatchZy
                return Response.ok("{\"status\":\"error\",\"message\":\"Error processing event\"}").build();
            }
            
            // Retorna 200 OK - o MatchZy espera uma resposta 2XX
            return Response.ok("{\"status\":\"ok\",\"message\":\"Event processed successfully\"}").build();

        } catch (Exception e) {
            System.out.println("Erro geral ao receber evento do MatchZy: " + e.getMessage());
            e.printStackTrace();
            // Retorna OK mesmo com erro para não interromper o MatchZy
            return Response.ok("{\"status\":\"error\",\"message\":\"Internal server error\"}").build();
        }
    }

    // ============================================================================
    // ENDPOINT LEGADO PARA LOGS DO CS2 (logaddress_add_http)
    // ============================================================================
    
    /**
     * Recebe logs do servidor CS2 via logaddress_add_http (endpoint legado)
     * Este endpoint será chamado pelo servidor CS2 sempre que houver eventos
     * Os logs vêm como texto puro do console do servidor
     * 
     * Comando no servidor CS2:
     * logaddress_add_http http://seu-servidor:porta/PhbChampReloaded/resources/gsi
     */
    
    
    /**
     * Verifica se o conteúdo parece ser um evento do MatchZy
     */
    private boolean isMatchZyEvent(String content) {
        if (content == null || content.trim().isEmpty()) {
            return false;
        }
        
        // Verifica se contém campos típicos de eventos do MatchZy
        String lowerContent = content.toLowerCase();
        return (lowerContent.contains("\"event\"") || lowerContent.contains("'event'")) &&
               (lowerContent.contains("match_start") || lowerContent.contains("round_start") ||
                lowerContent.contains("round_end") || lowerContent.contains("kill") ||
                lowerContent.contains("bomb_planted") || lowerContent.contains("match_end"));
    }
    
    /**
     * Extrai JSON do meio de logs do CS2
     * O CS2 às vezes envia logs misturados com JSON
     */
    

    // ============================================================================
    // ENDPOINTS DE TESTE E INFORMAÇÃO
    // ============================================================================

    /**
     * Endpoint de teste - também processa logs como o endpoint principal
     * Mantido para compatibilidade
     */
   

    /**
     * Endpoint GET para testar se o serviço está funcionando
     */
   

    /**
     * Endpoint raiz GET para verificar se o recurso está acessível
     */
    @jakarta.ws.rs.GET
    @Produces(MediaType.TEXT_PLAIN)
    public Response root() {
        return Response.ok("CS2 GSI Resource is accessible.\n\n" +
                          "Endpoints:\n" +
                          "- POST /gsi/matchzy - Recebe eventos JSON do MatchZy\n" +
                          "- POST /gsi - Recebe logs do CS2 (legado)\n\n" +
                          "Configuração do MatchZy:\n" +
                          "sm_matchzy_events_api_url \"http://seu-servidor:porta/PhbChampReloaded/resources/gsi/matchzy\"\n\n" +
                          "Configuração do CS2 (legado):\n" +
                          "logaddress_add_http http://seu-servidor:porta/PhbChampReloaded/resources/gsi").build();
    }
}
