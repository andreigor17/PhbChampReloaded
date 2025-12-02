/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.champ.Utilitario;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 *
 * @author andre
 */
public class APIPath {

    public static String pathToAPI() throws FileNotFoundException, IOException {

        JSONParser parser = new JSONParser();
        String path = null;

        String pathUbuntu = "/opt/apipath.json";
        String pathWindows = "C:\\apipath.json";

        File ubuntuFile = new File(pathUbuntu);
        File windowsFile = new File(pathWindows);

        if (ubuntuFile.exists()) {
            path = pathUbuntu;
        } else {
            path = pathWindows;
        }

        if (!ubuntuFile.exists() && !windowsFile.exists()) {
            Mensagem.fatal("Nenhum arquivo de configuração encontrado!");
            return null;
        }

        Reader reader = new FileReader(path);

        JSONObject jsonObject = null;
        try {
            jsonObject = (JSONObject) parser.parse(reader);
        } catch (org.json.simple.parser.ParseException ex) {
            Logger.getLogger(APIPath.class.getName()).log(Level.SEVERE, null, ex);
        }
        //System.out.println(jsonObject);

        String caminho_api = (String) jsonObject.get("caminho_api");
        //System.out.println(caminho_api);

        if (caminho_api != null) {
            return caminho_api;
        } else {
            return null;
        }

    }

    public static void writerPath(String path) {

        //Cria um Objeto JSON
        JSONObject jsonObject = new JSONObject();

        FileWriter writeFile = null;

        //Armazena dados em um Objeto JSON
        jsonObject.put("caminho_api", path);

        try {
            writeFile = new FileWriter("/opt/apipath.json");
            //Escreve no arquivo conteudo do Objeto JSON
            writeFile.write(jsonObject.toJSONString());
            writeFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Imprimne na Tela o Objeto JSON para vizualização
        //System.out.println(jsonObject);

    }

    public static void updateFile(String newPath) throws IOException {

        String path = pathToAPI();
        String replace = path.replace(path, newPath);
        writerPath(replace);

    }

}
