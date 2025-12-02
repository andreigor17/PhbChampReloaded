package br.com.champ.Servico;

import br.com.champ.Enums.Url;
import br.com.champ.Modelo.Anexo;
import br.com.champ.Utilitario.APIPath;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
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
    public static final String REAL_PATH_TMP = "/opt/uploads/tmp/";
    public static final String REAL_PATH_TMP_WINDOWS = "D:\\Temp\\images\\";
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
            return REAL_PATH_OPT;
        } else {
            return REAL_PATH_OPT_WINDOWS;
        }
    }

    public static String getRealPathTmp() {
        if (SystemUtils.IS_OS_LINUX || SystemUtils.IS_OS_MAC || SystemUtils.IS_OS_MAC_OSX) {
            return REAL_PATH_TMP;
        } else {
            return REAL_PATH_TMP_WINDOWS;
        }
    }

    public Anexo fileUpload(FileUploadEvent event) {

        Anexo a = new Anexo();
        File tempFile = null;
        try {
            String osPath = null;

            osPath = getRealPath();

            File path = new File(osPath);
            if (!path.exists()) {
                path.mkdirs();
            }

            this.nome = event.getFile().getFileName();
            String fileName = generateFileNameWithTimestamp() + this.nome;
            tempFile = new File(path, fileName);
            
            // Salva o arquivo em disco usando streaming (evita OutOfMemoryError)
            try (InputStream inputStream = event.getFile().getInputStream();
                 FileOutputStream outputStream = new FileOutputStream(tempFile)) {
                
                byte[] buffer = new byte[8192]; // Buffer de 8KB
                int bytesRead;
                long totalBytes = 0;
                
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                    totalBytes += bytesRead;
                    
                    // Log a cada 10MB para monitorar progresso
                    if (totalBytes % (10 * 1024 * 1024) == 0) {
                        System.out.println("Upload progresso: " + (totalBytes / 1024 / 1024) + " MB");
                    }
                }
                
                System.out.println("Arquivo salvo em disco: " + tempFile.getAbsolutePath() + " (" + (totalBytes / 1024 / 1024) + " MB)");
            }

            this.caminho = path.getAbsolutePath() + "/";
            
            // Para arquivos grandes, faz upload direto para a API usando multipart
            long fileSize = tempFile.length();
            
            if (fileSize > 10 * 1024 * 1024) { // Arquivos maiores que 10MB
                // Upload direto para API usando multipart/form-data (streaming)
                a = uploadFileToAPI(tempFile, this.nome, fileName);
            } else {
                // Para arquivos menores, lê na memória normalmente
                byte[] fileContent = readFileToByteArray(tempFile);
                this.arquivo = fileContent;
                
                a.setNome(REAL_PATH_OPT + fileName);
                a.setNomeExibicao(this.nome);
                a.setImagem(fileContent);
                
                // Salva na API
                a = save(a, null, Url.SALVAR_ANEXO.getNome());
            }
            
            // Se salvou com sucesso, mantém o arquivo em disco
            // Se não, remove o arquivo temporário
            if (a == null || a.getId() == null) {
                if (tempFile.exists()) {
                    tempFile.delete();
                }
            }

        } catch (OutOfMemoryError e) {
            System.err.println("Erro de memória ao processar arquivo muito grande: " + e.getMessage());
            if (tempFile != null && tempFile.exists()) {
                try {
                    tempFile.delete();
                } catch (Exception deleteEx) {
                    System.err.println("Erro ao deletar arquivo temporário: " + deleteEx.getMessage());
                }
            }
            throw new RuntimeException("Arquivo muito grande para processar. Tente um arquivo menor ou aumente a memória do servidor.", e);
        } catch (Exception ex) {
            System.err.println("Erro no upload do arquivo: " + ex.getMessage());
            ex.printStackTrace();
            Logger.getLogger(AnexoServico.class.getName()).log(Level.SEVERE, "Erro detalhado no upload", ex);
            if (tempFile != null && tempFile.exists()) {
                try {
                    tempFile.delete();
                } catch (Exception deleteEx) {
                    System.err.println("Erro ao deletar arquivo temporário: " + deleteEx.getMessage());
                }
            }
            // Re-lança a exceção para que o ManagerDemo possa tratá-la
            throw ex;
        }
        return a;
    }
    
    /**
     * Lê arquivo do disco em chunks para evitar OutOfMemoryError
     * Limite: 10MB para evitar problemas de memória
     */
    private byte[] readFileToByteArray(File file) throws IOException {
        long fileSize = file.length();
        
        // Para arquivos maiores que 10MB, lança exceção
        if (fileSize > 10 * 1024 * 1024) {
            throw new IOException("Arquivo muito grande (" + (fileSize / 1024 / 1024) + " MB). " +
                    "Use upload direto para a API.");
        }
        
        try (FileInputStream fis = new FileInputStream(file);
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            
            byte[] buffer = new byte[8192];
            int bytesRead;
            
            while ((bytesRead = fis.read(buffer)) != -1) {
                baos.write(buffer, 0, bytesRead);
            }
            
            return baos.toByteArray();
        }
    }
    
    /**
     * Faz upload direto do arquivo para a API usando multipart/form-data com streaming
     * Evita carregar o arquivo inteiro na memória
     */
    private Anexo uploadFileToAPI(File file, String originalFileName, String fileName) throws Exception {
        try {
            String apiPath = pathToAPI();
            if (apiPath == null || apiPath.isEmpty()) {
                throw new Exception("Caminho da API não configurado. Verifique o arquivo apipath.json");
            }
            
            String url = apiPath + "/api/demo/upload";
            System.out.println("Fazendo upload para: " + url);
            
            String boundary = "----WebKitFormBoundary" + System.currentTimeMillis();
            
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
            connection.setRequestProperty("Connection", "keep-alive");
            
            // Timeouts aumentados para arquivos grandes
            connection.setConnectTimeout(30000); // 30 segundos
            connection.setReadTimeout(300000); // 5 minutos para uploads grandes
            
            try (OutputStream outputStream = connection.getOutputStream();
                 FileInputStream fileInputStream = new FileInputStream(file)) {
                
                // Escreve o campo do arquivo
                String fileField = "--" + boundary + "\r\n" +
                    "Content-Disposition: form-data; name=\"file\"; filename=\"" + originalFileName + "\"\r\n" +
                    "Content-Type: application/octet-stream\r\n\r\n";
                
                outputStream.write(fileField.getBytes("UTF-8"));
                
                // Copia o arquivo em chunks (streaming)
                byte[] buffer = new byte[8192];
                int bytesRead;
                long totalBytes = 0;
                
                while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                    totalBytes += bytesRead;
                }
                
                // Fecha o multipart
                String endBoundary = "\r\n--" + boundary + "--\r\n";
                outputStream.write(endBoundary.getBytes("UTF-8"));
                
                System.out.println("Arquivo enviado para API: " + (totalBytes / 1024 / 1024) + " MB");
            }
            
            // Lê a resposta
            int responseCode = connection.getResponseCode();
            System.out.println("Resposta da API: HTTP " + responseCode);
            
            if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED) {
                String response = readResponse(connection);
                System.out.println("Resposta da API: " + response);
                
                Gson gson = new Gson();
                
                // A resposta pode vir como um Map com o anexo dentro
                java.util.Map<String, Object> responseMap = gson.fromJson(response, 
                    new TypeToken<java.util.Map<String, Object>>() {}.getType());
                
                if (responseMap != null) {
                    Object anexoIdObj = responseMap.get("anexoId");
                    if (anexoIdObj != null) {
                        // Busca o anexo criado
                        Long anexoId;
                        if (anexoIdObj instanceof Double) {
                            anexoId = ((Double) anexoIdObj).longValue();
                        } else if (anexoIdObj instanceof Integer) {
                            anexoId = ((Integer) anexoIdObj).longValue();
                        } else {
                            anexoId = Long.parseLong(anexoIdObj.toString());
                        }
                        
                        Anexo anexo = new Anexo();
                        anexo.setId(anexoId);
                        anexo.setNome(REAL_PATH_OPT + fileName);
                        anexo.setNomeExibicao(originalFileName);
                        System.out.println("Upload concluído com sucesso! Anexo ID: " + anexoId);
                        return anexo;
                    } else {
                        System.err.println("Resposta da API não contém anexoId");
                        throw new Exception("Resposta da API não contém anexoId. Resposta: " + response);
                    }
                } else {
                    System.err.println("Resposta da API é null ou inválida");
                    throw new Exception("Resposta inválida da API: " + response);
                }
            } else {
                String errorResponse = readErrorResponse(connection);
                System.err.println("Erro HTTP " + responseCode + ": " + errorResponse);
                throw new Exception("Erro ao fazer upload: HTTP " + responseCode + " - " + errorResponse);
            }
            
        } catch (Exception e) {
            Logger.getLogger(AnexoServico.class.getName()).log(Level.SEVERE, "Erro ao fazer upload direto para API", e);
            throw e;
        }
        
        return null;
    }
    
    /**
     * Lê resposta de erro da conexão HTTP
     */
    private String readErrorResponse(HttpURLConnection connection) throws IOException {
        try (InputStream errorStream = connection.getErrorStream()) {
            if (errorStream != null) {
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = errorStream.read(buffer)) != -1) {
                    os.write(buffer, 0, bytesRead);
                }
                return new String(os.toByteArray());
            }
        }
        return "Sem detalhes do erro";
    }

    public Anexo fileUploadTemp(FileUploadEvent event) {

        Anexo a = new Anexo();
        try {
            String osPath = null;          

            osPath = getRealPathTmp();

            File path = new File(osPath);
            if (!path.exists()) {
                path.mkdir();
            }

            this.caminho = path.getAbsolutePath() + "/";
            this.arquivo = event.getFile().getContent();
            this.nome = generateFileNameWithTimestamp() + event.getFile().getFileName();

            a.setNome(REAL_PATH_TMP + this.nome);
            a.setNomeExibicao(this.nome);
            gravar();

        } catch (Exception ex) {
            System.out.println("Erro no upload do arquivo" + ex);
        }
        return a;
    }

    public static String generateFileNameWithTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmssSSS", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        String formattedNow = sdf.format(new GregorianCalendar().getTime());

        return formattedNow;
    }

    public Anexo salvarAnexo(Anexo anexo) throws Exception {
        try {
            Anexo a = new Anexo();
            a.setNome(REAL_PATH_OPT + generateFileNameWithTimestamp() + anexo.getNomeExibicao());
            a.setNomeExibicao(anexo.getNomeExibicao());
            try {
                a = save(a, null, Url.SALVAR_ANEXO.getNome());
            } catch (Exception ex) {
                Logger.getLogger(AnexoServico.class.getName()).log(Level.SEVERE, null, ex);
            }

            gravar(anexo.getNome(), a.getNome());
//            if (a.getId() == null) {
//                a = save(a, null, Url.SALVAR_ANEXO.getNome());
//            }
            return a;

        } catch (IOException ex) {
            Logger.getLogger(AnexoServico.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;

    }

    public void gravar(String oldPath, String newPath) throws IOException {

        // Abertura dos streams de leitura e escrita
        FileInputStream fis = new FileInputStream(oldPath);
        FileOutputStream fos = new FileOutputStream(newPath);

        // Leitura e gravação dos dados
        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = fis.read(buffer)) != -1) {
            fos.write(buffer, 0, bytesRead);
        }

        // Fechamento dos streams
        fis.close();
        fos.close();

    }

    public void gravar() {

        try {

            FileOutputStream fos;
            fos = new FileOutputStream(this.caminho + this.nome);
            fos.write(this.arquivo);
            //System.out.println("caminho do arquivo " + this.caminho);
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
        System.err.println(url);
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

    /**
     * Busca todos os anexos
     */
    public List<Anexo> findAll(Anexo anexo, String uri) throws Exception {
        try {
            String url = pathToAPI() + uri;
            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("Content-Type", "application/json");
            con.setRequestProperty("Accept", "application/json");
            
            int responseCode = con.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String inputLine;
                StringBuffer response = new StringBuffer();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                
                Gson gson = new Gson();
                Type listType = new TypeToken<ArrayList<Anexo>>() {}.getType();
                List<Anexo> anexos = gson.fromJson(response.toString(), listType);
                return anexos != null ? anexos : new ArrayList<>();
            }
        } catch (IOException | JSONException e) {
            Logger.getLogger(AnexoServico.class.getName()).log(Level.SEVERE, "Erro ao buscar anexos", e);
        }
        return new ArrayList<>();
    }

    /**
     * Busca um anexo por ID
     */
    public Anexo find(Anexo anexo, Long id, String uri) throws Exception {
        try {
            String url = pathToAPI() + uri + (id != null ? id : anexo.getId());
            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("Content-Type", "application/json");
            con.setRequestProperty("Accept", "application/json");
            
            int responseCode = con.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String inputLine;
                StringBuffer response = new StringBuffer();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                
                Gson gson = new Gson();
                return gson.fromJson(response.toString(), Anexo.class);
            }
        } catch (IOException | JSONException e) {
            Logger.getLogger(AnexoServico.class.getName()).log(Level.SEVERE, "Erro ao buscar anexo", e);
        }
        return null;
    }

    /**
     * Deleta um anexo
     */
    public void delete(Anexo anexo, Long id, String uri) throws Exception {
        try {
            String url = pathToAPI() + uri + (id != null ? id : anexo.getId());
            HttpURLConnection request = (HttpURLConnection) new URL(url).openConnection();
            request.setRequestMethod("DELETE");
            request.setRequestProperty("Content-Type", "application/json");
            request.connect();
            
            int responseCode = request.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK && responseCode != HttpURLConnection.HTTP_NO_CONTENT) {
                throw new Exception("Erro ao deletar anexo. Código: " + responseCode);
            }
            
            request.disconnect();
        } catch (IOException e) {
            Logger.getLogger(AnexoServico.class.getName()).log(Level.SEVERE, "Erro ao deletar anexo", e);
            throw new Exception("Erro ao deletar anexo: " + e.getMessage());
        }
    }

}
