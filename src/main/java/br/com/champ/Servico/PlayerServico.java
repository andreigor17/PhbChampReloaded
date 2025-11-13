/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.champ.Servico;

import br.com.champ.Enums.Url;
import br.com.champ.Modelo.Player;
import br.com.champ.Utilitario.APIPath;
import br.com.champ.vo.PlayerSteamVo;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author andre
 */
@Stateless
public class PlayerServico implements Serializable {

    @EJB
    private ConfiguracaoServico configuracaoServico;

    public String pathToAPI() throws IOException {
        return APIPath.pathToAPI();

    }

    public List<Player> pesquisar(String nomePlayer) throws Exception {

        try {
            String url = pathToAPI() + Url.BUSCAR_PLAYER_NOME.getNome() + "?nomePlayer=";
            Gson gson = new Gson();
            url += nomePlayer;
            //System.err.println("url " + url);
            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            // optional default is GET
            con.setRequestMethod("GET");
            //add request header
            con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            con.setRequestProperty("Accept", "application/json");
            con.setRequestProperty("Accept-Charset", "UTF-8");
            int responseCode = con.getResponseCode();
            //System.out.println("\nSending 'GET' request to URL : " + url);
            //System.out.println("Response Code : " + responseCode);
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream(), java.nio.charset.StandardCharsets.UTF_8));
            String inputLine;
            StringBuffer response = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            //print in String
            //System.out.println(response.toString());
            //Read JSON response and print           
            List<Player> p = new ArrayList<>();

            //Player[] userArray = gson.fromJson(response.toString(), Player[].class);
            Type userListType = new TypeToken<ArrayList<Player>>() {
            }.getType();

            ArrayList<Player> userArray = gson.fromJson(response.toString(), userListType);

            for (Player user : userArray) {
                p.add(user);
            }

            return p;
        } catch (IOException iOException) {
            System.err.println(iOException);
        } catch (JSONException jSONException) {
            System.err.println(jSONException);
        } catch (NumberFormatException numberFormatException) {
            System.err.println(numberFormatException);
        }
        return null;

    }

    public Player buscaPlayer(Long id) {
        try {
            String url = pathToAPI() + Url.BUSCAR_PLAYER_ID.getNome() + id;
            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            // optional default is GET
            con.setRequestMethod("GET");
            //add request header
            con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            con.setRequestProperty("Accept", "application/json");
            con.setRequestProperty("Accept-Charset", "UTF-8");
            int responseCode = con.getResponseCode();
            //System.out.println("Response Code : " + responseCode);
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream(), java.nio.charset.StandardCharsets.UTF_8));
            String inputLine;
            StringBuffer response = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            //print in String
            System.out.println(response.toString());
            //Read JSON response and print
            Gson gson = new Gson();
            Player p1 = new Player();

            Player userArray = gson.fromJson(response.toString(), Player.class);

            p1 = userArray;
            return p1;
        } catch (IOException iOException) {
            System.err.println(iOException);
        } catch (JSONException jSONException) {
            System.err.println(jSONException);
        } catch (NumberFormatException numberFormatException) {
            System.err.println(numberFormatException);
        }
        return null;

    }

    public Player buscaPlayerSteamId(String stemId) {
        try {
            String url = pathToAPI() + Url.BUSCAR_PLAYER_STEAM_ID.getNome() + stemId;
            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            // optional default is GET
            con.setRequestMethod("GET");
            //add request header
            con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            con.setRequestProperty("Accept", "application/json");
            con.setRequestProperty("Accept-Charset", "UTF-8");
            int responseCode = con.getResponseCode();
            if (responseCode == 404) {
                return null;
            }
            System.out.println("Response Code : " + responseCode);
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream(), java.nio.charset.StandardCharsets.UTF_8));
            String inputLine;
            StringBuffer response = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            //print in String
            //System.out.println(response.toString());
            //Read JSON response and print
            Gson gson = new Gson();
            Player p1 = new Player();

            Player userArray = gson.fromJson(response.toString(), Player.class);

            p1 = userArray;
            return p1;
        } catch (IOException iOException) {
            System.err.println(iOException);
        } catch (JSONException jSONException) {
            System.err.println(jSONException);
        } catch (NumberFormatException numberFormatException) {
            System.err.println(numberFormatException);
        }
        return null;

    }

    public List<Player> autoCompletePessoa() {
        return buscaPlayers();
    }

    public List<Player> buscaPlayers() {
        try {
            String url = pathToAPI() + Url.BUSCAR_PLAYERS.getNome();
            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            // optional default is GET
            con.setRequestMethod("GET");
            //add request header
            con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            con.setRequestProperty("Accept", "application/json");
            con.setRequestProperty("Accept-Charset", "UTF-8");
            int responseCode = con.getResponseCode();
            //System.out.println("\nSending 'GET' request to URL : " + url);
            //System.out.println("Response Code : " + responseCode);
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream(), java.nio.charset.StandardCharsets.UTF_8));
            String inputLine;
            StringBuffer response = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            //print in String
            //System.out.println(response.toString());
            //Read JSON response and print
            Gson gson = new Gson();
            List<Player> p = new ArrayList<>();

            //Player[] userArray = gson.fromJson(response.toString(), Player[].class);
            Type userListType = new TypeToken<ArrayList<Player>>() {
            }.getType();

            ArrayList<Player> userArray = gson.fromJson(response.toString(), userListType);

            for (Player user : userArray) {
                p.add(user);
            }

            return p;
        } catch (IOException iOException) {
            System.err.println(iOException);
        } catch (JSONException jSONException) {
            System.err.println(jSONException);
        } catch (NumberFormatException numberFormatException) {
            System.err.println(numberFormatException);
        }
        return null;
    }

    public Player save(Player player, Long id, String uri) throws Exception {

        String url;
        if (id != null) {
            url = pathToAPI() + uri + id;
        } else {
            url = pathToAPI() + uri;
        }
        try {
            // Cria um objeto HttpURLConnection:
            HttpURLConnection request = (HttpURLConnection) new URL(url).openConnection();

            try {
                // Define que a conexão pode enviar informações e obtê-las de volta:
                request.setDoOutput(true);
                request.setDoInput(true);

                // Define o content-type com charset UTF-8:
                request.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                request.setRequestProperty("Accept-Charset", "UTF-8");

                // Define o método da requisição:
                if (id != null) {
                    request.setRequestMethod("PUT");
                } else {
                    request.setRequestMethod("POST");
                }

                // Conecta na URL:
                request.connect();
                Gson gson = new Gson();
                String json = gson.toJson(player);
                System.out.println("Json Player " + json);

                // Escreve o objeto JSON usando o OutputStream da requisição:
                try (OutputStream outputStream = request.getOutputStream()) {
                    outputStream.write(json.getBytes("UTF-8"));
                }

                Player p = new Player();
                Player userArray = gson.fromJson(readResponse(request), Player.class);
                p = userArray;
                return p;

                // Caso você queira usar o código HTTP para fazer alguma coisa, descomente esta linha.
                //int response = request.getResponseCode();
            } finally {
                request.disconnect();
            }
        } catch (IOException ex) {
            System.err.println(ex);
        }
        return null;
    }

    public Player registrarPlayer(Player player, Long id, String uri) throws Exception {

        String url;
        if (id != null) {
            url = pathToAPI() + uri + id;
        } else {
            url = pathToAPI() + uri;
        }
        try {
            // Cria um objeto HttpURLConnection:
            HttpURLConnection request = (HttpURLConnection) new URL(url).openConnection();

            try {
                // Define que a conexão pode enviar informações e obtê-las de volta:
                request.setDoOutput(true);
                request.setDoInput(true);

                // Define o content-type com charset UTF-8:
                request.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                request.setRequestProperty("Accept-Charset", "UTF-8");

                // Define o método da requisição:
                if (id != null) {
                    request.setRequestMethod("PUT");
                } else {
                    request.setRequestMethod("POST");
                }

                // Conecta na URL:
                request.connect();
                Gson gson = new Gson();
                String json = gson.toJson(player);
                System.out.println("Json Player " + json);

                // Escreve o objeto JSON usando o OutputStream da requisição:
                try (OutputStream outputStream = request.getOutputStream()) {
                    outputStream.write(json.getBytes("UTF-8"));
                }

                // Verifica o código de resposta
                int responseCode = request.getResponseCode();
                System.out.println("Response Code: " + responseCode);
                
                if (responseCode == 409) { // HTTP_CONFLICT
                    System.err.println("Erro: Login ou Steam ID já cadastrado");
                    throw new Exception("Este login ou Steam ID já está em uso. Por favor, escolha outro.");
                } else if (responseCode == 400) { // BAD_REQUEST
                    System.err.println("Erro: Dados inválidos");
                    throw new Exception("Dados inválidos. Verifique se preencheu todos os campos obrigatórios.");
                } else if (responseCode >= 400) {
                    System.err.println("Erro HTTP: " + responseCode);
                    throw new Exception("Erro ao registrar jogador. Código: " + responseCode);
                }

                Player p = new Player();
                Player userArray = gson.fromJson(readResponse(request), Player.class);
                p = userArray;
                return p;

            } finally {
                request.disconnect();
            }
        } catch (IOException ex) {
            System.err.println("Erro de comunicação: " + ex.getMessage());
            throw new Exception("Erro ao comunicar com o servidor: " + ex.getMessage());
        }
    }

    public Player delete(Player player, String uri) throws Exception {

        String url = pathToAPI() + uri + player.getId();

        try {
            // Cria um objeto HttpURLConnection:
            HttpURLConnection request = (HttpURLConnection) new URL(url).openConnection();

            try {
                // Define que a conexão pode enviar informações e obtê-las de volta:
                request.setDoOutput(true);
                request.setDoInput(true);

                // Define o content-type com charset UTF-8:
                request.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                request.setRequestProperty("Accept-Charset", "UTF-8");

                request.setRequestMethod("DELETE");

                // Conecta na URL:
                request.connect();
                Gson gson = new Gson();
                String json = gson.toJson(player);
                //System.out.println("Json Player " + json);

                // Escreve o objeto JSON usando o OutputStream da requisição:
                try (OutputStream outputStream = request.getOutputStream()) {
                    outputStream.write(json.getBytes("UTF-8"));
                }

                Player p = new Player();
                Player userArray = gson.fromJson(readResponse(request), Player.class);
                p = userArray;
                return p;

                // Caso você queira usar o código HTTP para fazer alguma coisa, descomente esta linha.
                //int response = request.getResponseCode();
            } finally {
                request.disconnect();
            }
        } catch (IOException ex) {
            System.err.println(ex);
        }
        return null;
    }

    private String readResponse(HttpURLConnection request) throws IOException {
        ByteArrayOutputStream os;
        try (InputStream is = request.getInputStream()) {
            os = new ByteArrayOutputStream();
            int b;
            while ((b = is.read()) != -1) {
                os.write(b);
            }
        }
        return new String(os.toByteArray(), java.nio.charset.StandardCharsets.UTF_8);
    }

    public PlayerSteamVo getPlayerInfo(String steamId64, String apiKey) {
        PlayerSteamVo playerSteamVo = new PlayerSteamVo();
        StringBuilder result = new StringBuilder();

        try {
            String urlString = "http://api.steampowered.com/ISteamUser/GetPlayerSummaries/v0002/?key=" + apiKey + "&steamids=" + steamId64;
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept-Charset", "UTF-8");

            BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream(), java.nio.charset.StandardCharsets.UTF_8));
            String line;
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }
            rd.close();

            // Parse JSON response
            JSONObject jsonResponse = new JSONObject(result.toString());
            JSONArray playersArray = jsonResponse.getJSONObject("response").getJSONArray("players");

            if (playersArray.length() > 0) {
                JSONObject player = playersArray.getJSONObject(0);
                System.err.println(player.toString());

                playerSteamVo.setSteamid(player.getString("steamid"));
                playerSteamVo.setPersonaname(player.getString("personaname"));
                playerSteamVo.setProfileurl(player.getString("profileurl"));
                playerSteamVo.setAvatar(player.getString("avatar"));
                playerSteamVo.setAvatarmedium(player.getString("avatarmedium"));
                playerSteamVo.setAvatarfull(player.getString("avatarfull"));
                if (player.has("realname")) {
                    playerSteamVo.setRealname(player.getString("realname"));
                }
                if (player.has("loccountrycode")) {
                    playerSteamVo.setLoccountrycode(player.getString("loccountrycode"));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return playerSteamVo;
    }

}
