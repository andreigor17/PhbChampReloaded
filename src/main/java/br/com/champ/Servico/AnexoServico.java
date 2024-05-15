package br.com.champ.Servico;

import br.com.champ.Enums.Url;
import br.com.champ.Modelo.Anexo;
import br.com.champ.Utilitario.APIPath;
import com.google.gson.Gson;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import org.apache.commons.lang.SystemUtils;
import org.json.JSONException;
import org.primefaces.event.FileUploadEvent;


/**
 *
 * @author andre
 */
@Stateless
public class AnexoServico {

    @EJB
    private ConfiguracaoServico configuracaoServico;

    private String diretorio;
    private String caminho;
    private byte[] arquivo;
    private String nome;
    public static final String REAL_PATH_OPT = "/opt/uploads/images/";
    public static final String REAL_PATH_OPT_WINDOWS = "D:\\uploads\\images\\";

    public String pathToAPI() throws IOException {
        return APIPath.pathToAPI();

    }

    public String getNome() {
        return nome;
    }

    public static String getRealPath() {
        if (SystemUtils.IS_OS_LINUX || SystemUtils.IS_OS_MAC || SystemUtils.IS_OS_MAC_OSX) {
            System.out.println("1");
            return REAL_PATH_OPT;
        } else {
            System.out.println("2");
            return REAL_PATH_OPT_WINDOWS;
        }
    }

    public Anexo fileUpload(FileUploadEvent event, String type) {

        Anexo a = new Anexo();
        try {
            String osPath = null;
            this.nome = event.getFile().getFileName() + type;

            osPath = getRealPath();

            File path = new File(osPath);
            if (!path.exists()) {
                path.mkdir();
            }

            this.caminho = path.getAbsolutePath() + "/";
            this.arquivo = event.getFile().getContent();
            this.nome = event.getFile().getFileName();

            a.setNome("/opt/uploads/images/" + event.getFile().getFileName());
            a.setNomeExibicao(event.getFile().getFileName());
            a = save(a, null, Url.SALVAR_ANEXO.getNome());
            gravar();

        } catch (Exception ex) {
            System.out.println("Erro no upload do arquivo" + ex);
        }
        return a;
    }

    public void gravar() {

        try {

            FileOutputStream fos;
            fos = new FileOutputStream(this.caminho + this.nome);
            fos.write(this.arquivo);
            System.out.println("caminho do arquivo " + this.caminho);
            fos.close();

        } catch (Exception ex) {
            System.out.println(ex);
        }

    }

    public Anexo buscaPlayer(Long id) {
        try {
            String url = pathToAPI() + "/anexos/" + id;
            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            // optional default is GET
            con.setRequestMethod("GET");
            //add request header
            con.setRequestProperty("Content-Type", "application/json");
            con.setRequestProperty("Accept", "application/json");
            int responseCode = con.getResponseCode();
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
            Anexo p1 = new Anexo();

            Anexo userArray = gson.fromJson(response.toString(), Anexo.class);

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

    public Anexo save(Anexo anexo, Long id, String uri) throws Exception {

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
                Gson gson = new Gson();
                String json = gson.toJson(anexo);
                System.out.println("Json Player " + json);

                // Escreve o objeto JSON usando o OutputStream da requisição:
                try (OutputStream outputStream = request.getOutputStream()) {
                    outputStream.write(json.getBytes("UTF-8"));
                }

                Anexo p = new Anexo();
                Anexo userArray = gson.fromJson(readResponse(request), Anexo.class);
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
        return new String(os.toByteArray());
    }

}
