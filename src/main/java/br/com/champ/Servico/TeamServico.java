package br.com.champ.Servico;

import br.com.champ.Modelo.Campeonato;
import br.com.champ.Modelo.Configuracao;
import br.com.champ.Modelo.Team;
import br.com.champ.Utilitario.APIPath;
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
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONException;


/**
 *
 * @author andre
 */
@Stateless
public class TeamServico {

    @EJB
    private ConfiguracaoServico configuracaoServico;

    public Configuracao obterConfiguracao() {
        return configuracaoServico.buscaConfig();

    }

    public String pathToAPI() throws IOException {
        return APIPath.pathToAPI();

    }

    public List<Team> pesquisar(String nomeTeam) throws Exception {
        // Validação: se nomeTeam for null ou vazio, retorna lista vazia
        if (nomeTeam == null || nomeTeam.trim().isEmpty()) {
            return new ArrayList<>();
        }

        try {
            // URL encode para tratar caracteres especiais
            String encodedNome = java.net.URLEncoder.encode(nomeTeam.trim(), "UTF-8");
            String url = pathToAPI() + "/api/team/team-nome?nomeTime=" + encodedNome;
            
            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            con.setRequestProperty("Accept", "application/json");
            con.setRequestProperty("Accept-Charset", "UTF-8");
            
            int responseCode = con.getResponseCode();
            
            // Se a resposta for diferente de 200, retorna lista vazia
            if (responseCode != HttpURLConnection.HTTP_OK) {
                System.err.println("Erro HTTP ao buscar team: " + responseCode);
                return new ArrayList<>();
            }
            
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream(), java.nio.charset.StandardCharsets.UTF_8));
            String inputLine;
            StringBuffer response = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            
            // Se a resposta estiver vazia, retorna lista vazia
            if (response.length() == 0) {
                return new ArrayList<>();
            }
            
            // Tenta fazer parse do JSON
            Gson gson = new Gson();
            Type userListType = new TypeToken<ArrayList<Team>>() {}.getType();
            ArrayList<Team> userArray = gson.fromJson(response.toString(), userListType);
            
            // Se o array for null, retorna lista vazia
            if (userArray == null) {
                return new ArrayList<>();
            }

            List<Team> t = new ArrayList<>();
            for (Team time : userArray) {
                if (time != null) {
                    t.add(time);
                }
            }

            return t;
        } catch (java.net.MalformedURLException e) {
            System.err.println("URL malformada ao buscar team: " + e.getMessage());
            return new ArrayList<>();
        } catch (IOException iOException) {
            System.err.println("Erro de I/O ao buscar team: " + iOException.getMessage());
            return new ArrayList<>();
        } catch (Exception e) {
            System.err.println("Erro ao buscar team: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public List<Team> autoCompleteTime() throws Exception {
        return buscaTimes();
    }

    public List<Team> buscaTimes() throws Exception {

        try {
            String url = pathToAPI() + "/api/team/teams-nao-amistosos";
            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            // optional default is GET
            con.setRequestMethod("GET");
            //add request header
            con.setRequestProperty("Content-Type", "application/json");
            con.setRequestProperty("Accept", "application/json");
            int responseCode = con.getResponseCode();
            System.out.println("\nSending 'GET' request to URL : " + url);
            System.out.println("Response Code : " + responseCode);
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
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
            List<Team> t = new ArrayList<>();

            //Team[] userArray = gson.fromJson(response.toString(), Team[].class);
            Type userListType = new TypeToken<ArrayList<Team>>() {
            }.getType();

            ArrayList<Team> userArray = gson.fromJson(response.toString(), userListType);

            for (Team time : userArray) {
                t.add(time);
            }

            return t;
        } catch (IOException iOException) {
            System.err.println(iOException);
        } catch (JSONException jSONException) {
            System.err.println(jSONException);
        } catch (NumberFormatException numberFormatException) {
            System.err.println(numberFormatException);
        }
        return null;

    }

    public Team buscaTeam(Long id) {
        try {
            String url = pathToAPI() + "/api/team/" + id;
            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            // optional default is GET
            con.setRequestMethod("GET");
            //add request header
            con.setRequestProperty("Content-Type", "application/json");
            con.setRequestProperty("Accept", "application/json");
            int responseCode = con.getResponseCode();
            System.out.println("\nSending 'GET' request to URL : " + url);
            System.out.println("Response Code : " + responseCode);
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
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
            Team t1 = new Team();

            Team userArray = gson.fromJson(response.toString(), Team.class);

            t1 = userArray;
            return t1;
        } catch (IOException iOException) {
            System.err.println(iOException);
        } catch (JSONException jSONException) {
            System.err.println(jSONException);
        } catch (NumberFormatException numberFormatException) {
            System.err.println(numberFormatException);
        }
        return null;

    }

    public Team buscaTeamPorCapitao(Long capitaoId) {
        try {
            String url = pathToAPI() + "/api/team/team-capitao/" + capitaoId;
            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("Content-Type", "application/json");
            con.setRequestProperty("Accept", "application/json");
            int responseCode = con.getResponseCode();
            System.out.println("\nSending 'GET' request to URL : " + url);
            System.out.println("Response Code : " + responseCode);
            
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(con.getInputStream()));
                String inputLine;
                StringBuffer response = new StringBuffer();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                
                Gson gson = new Gson();
                Team team = gson.fromJson(response.toString(), Team.class);
                return team;
            } else {
                // Time não encontrado (404 ou outro código)
                return null;
            }
        } catch (IOException iOException) {
            System.err.println(iOException);
        } catch (JSONException jSONException) {
            System.err.println(jSONException);
        } catch (NumberFormatException numberFormatException) {
            System.err.println(numberFormatException);
        }
        return null;
    }

    public Team save(Team team, Long id, String uri) throws Exception {

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

                // Define o content-type:
                request.setRequestProperty("Content-Type", "application/json");

                // Define o método da requisição:
                if (id != null) {
                    request.setRequestMethod("PUT");
                } else {
                    request.setRequestMethod("POST");
                }

                // Conecta na URL:
                request.connect();
                // Montando o  Json
                Gson gson = new Gson();
                String json = gson.toJson(team);
                System.out.println("Montagem do time: " + json);

                // Escreve o objeto JSON usando o OutputStream da requisição:
                try (OutputStream outputStream = request.getOutputStream()) {
                    outputStream.write(json.getBytes("UTF-8"));
                }

                Team t = new Team();

                Team userArray = gson.fromJson(readResponse(request), Team.class);
                t = userArray;

                return t;
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
        return new String(os.toByteArray());
    }
    
    /**
     * Busca os campeonatos vencidos por um time (troféus)
     * @param teamId ID do time
     * @return Lista de campeonatos onde o time foi campeão
     */
    public List<Campeonato> buscarTrofeus(Long teamId) {
        if (teamId == null) {
            return new ArrayList<>();
        }
        
        try {
            String url = pathToAPI() + "/api/team/" + teamId + "/trofeus";
            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            con.setRequestProperty("Accept", "application/json");
            con.setRequestProperty("Accept-Charset", "UTF-8");
            
            int responseCode = con.getResponseCode();
            System.out.println("\nSending 'GET' request to URL : " + url);
            System.out.println("Response Code : " + responseCode);
            
            if (responseCode != HttpURLConnection.HTTP_OK) {
                System.err.println("Erro HTTP ao buscar troféus: " + responseCode);
                return new ArrayList<>();
            }
            
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream(), java.nio.charset.StandardCharsets.UTF_8));
            String inputLine;
            StringBuffer response = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            
            if (response.length() == 0) {
                return new ArrayList<>();
            }
            
            Gson gson = new Gson();
            Type listType = new TypeToken<ArrayList<Campeonato>>() {}.getType();
            ArrayList<Campeonato> campeonatos = gson.fromJson(response.toString(), listType);
            
            if (campeonatos == null) {
                return new ArrayList<>();
            }
            
            return campeonatos;
        } catch (IOException e) {
            System.err.println("Erro de I/O ao buscar troféus: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        } catch (Exception e) {
            System.err.println("Erro ao buscar troféus: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

}
