/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package br.com.champ.Servico;

import br.com.champ.Modelo.Player;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author andrepc
 */
@Stateless
public class SteamAPIServico {

    private static final String API_KEY = "F10A919CE16995E066B463C9005AF4D3";
    private static final String API_BASE_URL = "https://api.steampowered.com";

    // Cache para armazenar resultados e evitar muitas chamadas à API
    private Map<String, CachedResponse> responseCache = new HashMap<>();

    public static void desabilitaVerificacaoSSL() throws Exception {
        TrustManager[] trustAllCerts = new TrustManager[]{
            new X509TrustManager() {
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return null;
                }

                public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
                }

                public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
                }
            }
        };

        SSLContext sc = SSLContext.getInstance("SSL");
        sc.init(null, trustAllCerts, new java.security.SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

        // Ignora hostname
        HostnameVerifier allHostsValid = (hostname, session) -> true;
        HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
    }

    /**
     * Obtém informações do perfil do jogador
     *
     * @param steamId64 O ID de 64 bits do Steam
     * @return JSONObject com os dados do perfil
     */
    public JSONObject getPlayerProfile(String steamId64) throws Exception {
        String endpoint = "/ISteamUser/GetPlayerSummaries/v2/";
        String params = "?key=" + API_KEY + "&steamids=" + steamId64;

        JSONObject response = makeRequest(endpoint + params);
        JSONArray players = response.getJSONObject("response").getJSONArray("players");

        if (players.length() > 0) {
            return players.getJSONObject(0);
        }

        return new JSONObject();
    }

    /**
     * Obtém o nível Steam do jogador
     *
     * @param steamId64 O ID de 64 bits do Steam
     * @return O nível Steam como um inteiro
     */
    public int getSteamLevel(String steamId64) throws Exception {
        String endpoint = "/IPlayerService/GetSteamLevel/v1/";
        String params = "?key=" + API_KEY + "&steamid=" + steamId64;

        JSONObject response = makeRequest(endpoint + params);
        return response.getJSONObject("response").getInt("player_level");
    }

    /**
     * Obtém a lista de jogos do jogador
     *
     * @param steamId64 O ID de 64 bits do Steam
     * @return JSONObject com a lista de jogos e contagens
     */
    public JSONObject getOwnedGames(String steamId64) throws Exception {
        String endpoint = "/IPlayerService/GetOwnedGames/v1/";
        String params = "?key=" + API_KEY + "&steamid=" + steamId64 + "&include_appinfo=true&include_played_free_games=true";

        return makeRequest(endpoint + params);
    }

    /**
     * Obtém a lista de amigos do jogador
     *
     * @param steamId64 O ID de 64 bits do Steam
     * @return JSONArray com a lista de amigos
     */
    public JSONArray getFriendsList(String steamId64) throws Exception {
        String endpoint = "/ISteamUser/GetFriendList/v1/";
        String params = "?key=" + API_KEY + "&steamid=" + steamId64 + "&relationship=friend";

        JSONObject response = makeRequest(endpoint + params);
        return response.getJSONObject("friendslist").getJSONArray("friends");
    }

    /**
     * Obtém estatísticas de um jogo específico para o jogador
     *
     * @param steamId64 O ID de 64 bits do Steam
     * @param appId O ID do aplicativo/jogo na Steam
     * @return JSONObject com as estatísticas do jogo
     */
    public JSONObject getGameStats(String steamId64, int appId) throws Exception {
        String endpoint = "/ISteamUserStats/GetUserStatsForGame/v2/";
        String params = "?key=" + API_KEY + "&steamid=" + steamId64 + "&appid=" + appId;

        return makeRequest(endpoint + params);
    }

    /**
     * Calcula o tempo total de jogo em horas
     *
     * @param steamId64 O ID de 64 bits do Steam
     * @return Tempo total de jogo em horas
     */
    public double getTotalPlaytime(String steamId64) throws Exception {
        JSONObject gamesResponse = getOwnedGames(steamId64);
        JSONArray games = gamesResponse.getJSONObject("response").getJSONArray("games");

        int totalMinutes = 0;
        for (int i = 0; i < games.length(); i++) {
            JSONObject game = games.getJSONObject(i);
            totalMinutes += game.getInt("playtime_forever");
        }

        return totalMinutes / 60.0; // Converter para horas
    }

    /**
     * Faz uma requisição HTTP para a API da Steam
     *
     * @param urlPath O caminho da URL após a base
     * @return JSONObject com a resposta
     */
    private JSONObject makeRequest(String urlPath) throws Exception {
        // Verificar cache primeiro
        if (responseCache.containsKey(urlPath)) {
            CachedResponse cached = responseCache.get(urlPath);
            if (System.currentTimeMillis() - cached.timestamp < TimeUnit.MINUTES.toMillis(15)) {
                return cached.response;
            }
        }

        int maxRetries = 2;
        int attempt = 0;

        while (attempt <= maxRetries) {
            desabilitaVerificacaoSSL();
            URL url = new URL(API_BASE_URL + urlPath);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuilder content = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    content.append(inputLine);
                }
                in.close();

                JSONObject response = new JSONObject(content.toString());

                // Armazenar no cache
                responseCache.put(urlPath, new CachedResponse(response));

                return response;
            } else if (responseCode == 429 && attempt < maxRetries) {
                // Espera exponencial simples (ex: 1s, 2s)
                Thread.sleep((long) Math.pow(2, attempt) * 1000);
                attempt++;
            } else {
                throw new Exception("Falha na requisição à API da Steam. Código: " + responseCode);
            }
        }

        throw new Exception("Falha após múltiplas tentativas com erro 429.");
    }

    /**
     * Classe interna para armazenar respostas em cache
     */
    private static class CachedResponse {

        long timestamp;
        JSONObject response;

        public CachedResponse(JSONObject response) {
            this.timestamp = System.currentTimeMillis();
            this.response = response;
        }
    }

    /**
     * Converte um SteamID no formato STEAM_X:Y:Z para o formato de 64 bits
     *
     * @param steamId O SteamID no formato STEAM_X:Y:Z
     * @return O SteamID no formato de 64 bits
     */
    public String convertSteamIdTo64(String steamId) {
        if (steamId.startsWith("STEAM_")) {
            String[] parts = steamId.split(":");
            if (parts.length == 3) {
                long y = Long.parseLong(parts[1]);
                long z = Long.parseLong(parts[2]);
                return String.valueOf(76561197960265728L + z * 2 + y);
            }
        }
        return steamId; // Retorna o mesmo ID se não for possível converter
    }

}
