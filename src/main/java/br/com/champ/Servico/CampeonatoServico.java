package br.com.champ.Servico;

import br.com.champ.Modelo.Campeonato;
import br.com.champ.Modelo.Configuracao;
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

/**
 *
 * @author andre
 */
import java.util.List;
import org.primefaces.shaded.json.JSONException;

;

@Stateless
public class CampeonatoServico {

    @EJB
    private ConfiguracaoServico configuracaoServico;

    public Configuracao obterConfiguracao() {
        return configuracaoServico.buscaConfig();

    }

    public String pathToAPI() throws IOException {
        return APIPath.pathToAPI();

    }

    public List<Campeonato> pesquisar() throws Exception {
        try {
            String url = pathToAPI() + "/api/campeonatos";
            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            // optional default is GET
            con.setRequestMethod("GET");
            //add request header
            con.setRequestProperty("Content-Type", "application/json");
            con.setRequestProperty("Accept", "application/json");
            int responseCode = con.getResponseCode();
            //System.out.println("\nSending 'GET' request to URL : " + url);
            //System.out.println("Response Code : " + responseCode);
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
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
            List<Campeonato> c = new ArrayList<>();

            //Team[] userArray = gson.fromJson(response.toString(), Team[].class);
            Type userListType = new TypeToken<ArrayList<Campeonato>>() {
            }.getType();

            ArrayList<Campeonato> userArray = gson.fromJson(response.toString(), userListType);

            for (Campeonato campeonato : userArray) {
                c.add(campeonato);
            }

            return c;
        } catch (IOException iOException) {
            System.err.println(iOException);
        } catch (JSONException jSONException) {
            System.err.println(jSONException);
        } catch (NumberFormatException numberFormatException) {
            System.err.println(numberFormatException);
        }
        return null;

    }

    public List<Campeonato> buscaCampPorPlayer(String uri, Long playerId, Long jogoId) {
        try {
            String url = pathToAPI() + uri + "?playerId=" + playerId;

            if (jogoId != null) {
                url = url + "&jogoId=" + jogoId;
            }

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
            System.out.println(response.toString());
            //Read JSON response and print
            Gson gson = new Gson();
            List<Campeonato> c = new ArrayList<>();

            //Team[] userArray = gson.fromJson(response.toString(), Team[].class);
            Type userListType = new TypeToken<ArrayList<Campeonato>>() {
            }.getType();

            ArrayList<Campeonato> userArray = gson.fromJson(response.toString(), userListType);

            for (Campeonato campeonato : userArray) {
                c.add(campeonato);
            }

            return c;
        } catch (IOException iOException) {
            System.err.println(iOException);
        } catch (JSONException jSONException) {
            System.err.println(jSONException);
        } catch (NumberFormatException numberFormatException) {
            System.err.println(numberFormatException);
        }
        return null;

    }

    public List<Campeonato> autoCompleteCamps() {
        return pesquisarCampsAtuais();
    }

    public List<Campeonato> pesquisarCampsAtuais() {
        try {
            String url = pathToAPI() + "/campeonatos";
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
            System.out.println(response.toString());
            //Read JSON response and print
            Gson gson = new Gson();
            List<Campeonato> c = new ArrayList<>();

            //Team[] userArray = gson.fromJson(response.toString(), Team[].class);
            Type userListType = new TypeToken<ArrayList<Campeonato>>() {
            }.getType();

            ArrayList<Campeonato> userArray = gson.fromJson(response.toString(), userListType);

            for (Campeonato campeonato : userArray) {
                c.add(campeonato);
            }

            return c;
        } catch (IOException iOException) {
            System.err.println(iOException);
        } catch (JSONException jSONException) {
            System.err.println(jSONException);
        } catch (NumberFormatException numberFormatException) {
            System.err.println(numberFormatException);
        }
        return null;

    }

    public Campeonato save(Campeonato camp, Long id, String uri) throws Exception {

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
                String json = gson.toJson(camp);
                System.out.println("Montagem do campeonato: " + json);

                // Escreve o objeto JSON usando o OutputStream da requisição:
                try (OutputStream outputStream = request.getOutputStream()) {
                    outputStream.write(json.getBytes("UTF-8"));
                }

                // Caso você queira usar o código HTTP para fazer alguma coisa, descomente esta linha.
                //int response = request.getResponseCode();            
                Campeonato c = new Campeonato();

                Campeonato userArray = gson.fromJson(readResponse(request), Campeonato.class);
                c = userArray;

                return c;
            } finally {
                request.disconnect();
            }
        } catch (IOException ex) {
            System.err.println(ex);
        }
        return null;
    }

    public Campeonato delete(Campeonato camp, String uri) throws Exception {

        String url = pathToAPI() + uri + camp.getId();

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
                request.setRequestMethod("DELETE");

                // Conecta na URL:
                request.connect();
                // Montando o  Json
                Gson gson = new Gson();
                String json = gson.toJson(camp);

                // Escreve o objeto JSON usando o OutputStream da requisição:
                try (OutputStream outputStream = request.getOutputStream()) {
                    outputStream.write(json.getBytes("UTF-8"));
                }

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

    public Campeonato buscaCamp(Long id) {
        try {
            String url = pathToAPI() + "/campeonatos/" + id;
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
            System.out.println(response.toString());
            //Read JSON response and print
            Gson gson = new Gson();
            Campeonato c = new Campeonato();

            Campeonato userArray = gson.fromJson(response.toString(), Campeonato.class);

            c = userArray;
            return c;
        } catch (IOException iOException) {
            System.err.println(iOException);
        } catch (JSONException jSONException) {
            System.err.println(jSONException);
        } catch (NumberFormatException numberFormatException) {
            System.err.println(numberFormatException);
        }
        return null;

    }

}
