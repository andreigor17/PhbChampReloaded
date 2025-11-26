/*
 * Resource JAX-RS para receber dados do Game State Integration (GSI) do CS2
 */
package br.com.irontech.phbchampreloaded.resources;

import br.com.champ.Servico.GameStateParser;
import br.com.champ.Servico.GameStateService;
import br.com.champ.vo.MatchData;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ejb.EJB;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
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

    /**
     * Recebe dados do GSI via POST Este endpoint será chamado pelo CS2 sempre
     * que houver mudanças no estado do jogo
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public Response receiveGameState(InputStream inputStream) {
        try {
            // Lê o JSON do stream
            String jsonString = new BufferedReader(
                    new InputStreamReader(inputStream, StandardCharsets.UTF_8))
                    .lines()
                    .collect(Collectors.joining("\n"));

            // Log para debug (pode remover depois)
            System.out.println("=== GSI Data Received ===");
            System.out.println(jsonString);
            System.out.println("=========================");

            // Parse do JSON para MatchData
            MatchData matchData = gameStateParser.parseGSIJson(jsonString);

            // Atualiza o serviço com os novos dados
            gameStateService.updateGameState(matchData);

            // Retorna 200 OK - o jogo espera uma resposta 2XX
            return Response.ok("OK").build();

        } catch (Exception e) {
            e.printStackTrace();
            // Retorna erro mas aceita para não interromper o GSI
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error: " + e.getMessage())
                    .build();
        }
    }

    /**
     * Endpoint simples para testar se o serviço está funcionando
     */
    @POST
    @Path("/test")
    public Response testPOST(
            String payload,
            @Context HttpServletRequest request,
            @Context HttpHeaders headers
    ) {
        try {
            System.out.println("=== EVENTO CS2 RECEBIDO ===");

            // Tentar converter para JSON
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = null;

            try {
                root = mapper.readTree(payload);
            } catch (Exception e) {
                System.out.println("Payload NÃO é JSON. Conteúdo bruto:");
                System.out.println(payload);
                System.out.println("=====================================");
                return Response.ok().build();
            }

            // MAPA
            if (root.has("map")) {
                JsonNode map = root.get("map");
                String mapName = map.has("name") ? map.get("name").asText() : "desconhecido";
                int round = map.has("round") ? map.get("round").asInt() : -1;

                System.out.println("Mapa: " + mapName);
                System.out.println("Round atual: " + (round >= 0 ? round : "indisponível"));
            }

            // PLAYERS
            if (root.has("players")) {
                JsonNode players = root.get("players");

                System.out.println("--- Jogadores CT ---");
                if (players.has("CT")) {
                    for (JsonNode ct : players.get("CT")) {
                        String name = ct.has("name") ? ct.get("name").asText() : "n/a";
                        int hp = ct.has("health") ? ct.get("health").asInt() : -1;
                        String steam = ct.has("steamid") ? ct.get("steamid").asText() : "n/a";

                        System.out.printf("CT: %s | HP: %d | SteamID: %s%n", name, hp, steam);
                    }
                }

                System.out.println("--- Jogadores Terror ---");
                if (players.has("T")) {
                    for (JsonNode t : players.get("T")) {
                        String name = t.has("name") ? t.get("name").asText() : "n/a";
                        int hp = t.has("health") ? t.get("health").asInt() : -1;
                        String steam = t.has("steamid") ? t.get("steamid").asText() : "n/a";

                        System.out.printf("TR: %s | HP: %d | SteamID: %s%n", name, hp, steam);
                    }
                }
            }

            System.out.println("=====================================");
            return Response.ok("OK").build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError().build();
        }
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
