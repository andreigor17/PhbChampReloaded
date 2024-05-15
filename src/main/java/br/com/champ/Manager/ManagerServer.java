/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package br.com.champ.Manager;

import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 *
 * @author andre
 */
@ViewScoped
@Named
public class ManagerServer implements Serializable {

    private static final Logger log = Logger.getLogger(ManagerServer.class.getName());
    private List<String> returns;
    private String csgoServerReturn;

    @PostConstruct
    public void init() {
        instanciar();
        try {
            //this.returns = executeShellHDSize();
        } catch (Exception e) {
            System.err.println(e);
        }
    }

    public void instanciar() {
        this.returns = new ArrayList<>();
    }

    public void iniciarSteamCmd() {
        String os = System.getProperty("os.name").toLowerCase();
        String steamCmdPath = "";
        String csgoPath = "";

        if (os.contains("win")) {
            steamCmdPath = "D:\\SteamCMD\\steamcmd.exe"; // Caminho para o executável do SteamCMD no Windows
            csgoPath = "C:\\CSGO"; // Pasta onde o CS:GO foi instalado no Windows
        } else if (os.contains("nix") || os.contains("nux") || os.contains("mac")) {
            steamCmdPath = "/home/andre/Steam/"; // Caminho para o executável do SteamCMD no Linux
            csgoPath = "/path/to/csgo"; // Pasta onde o CS:GO foi instalado no Linux
        }

        String steamCmdCommand = steamCmdPath + " +login anonymous +force_install_dir " + csgoPath + " +app_update 740 validate +quit";

        try {
            ProcessBuilder processBuilder = new ProcessBuilder("bash", "-c", steamCmdCommand);
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            int exitCode = process.waitFor();
            if (exitCode == 0) {
                csgoServerReturn = "Servidor CS:GO iniciado com sucesso!";
            } else {
                csgoServerReturn = "Ocorreu um erro ao iniciar o servidor CS:GO.";
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public List<String> executeCommand(final String command) throws IOException {

        final ArrayList<String> commands = new ArrayList<String>();
        List<String> returns = new ArrayList<String>();
        commands.add("/bin/bash");
        commands.add("-c");
        commands.add(command);

        BufferedReader br = null;

        try {
            final ProcessBuilder p = new ProcessBuilder(commands);
            final Process process = p.start();
            final InputStream is = process.getInputStream();
            final InputStreamReader isr = new InputStreamReader(is);
            br = new BufferedReader(isr);

            String line;
            while ((line = br.readLine()) != null) {
                if (line.contains("disk1s1")) {
                    System.out.println("Retorno do comando = [" + line + "]");
                    returns.add(line);
                }
            }
        } catch (IOException ioe) {
            log.severe("Erro ao executar comando shell" + ioe.getMessage());
            throw ioe;
        } finally {
            secureClose(br);
        }
        return returns;
    }

    private void secureClose(final Closeable resource) {
        try {
            if (resource != null) {
                resource.close();
            }
        } catch (IOException ex) {
            log.severe("Erro = " + ex.getMessage());
        }
    }

    public List<String> executeShellHDSize() throws IOException {
        List<String> returns = new ArrayList<>();
        returns = executeCommand("df -h");
        return returns;
    }

    public List<String> getReturns() {
        return returns;
    }

    public void setReturns(List<String> returns) {
        this.returns = returns;
    }

    public String getCsgoServerReturn() {
        return csgoServerReturn;
    }

    public void setCsgoServerReturn(String csgoServerReturn) {
        this.csgoServerReturn = csgoServerReturn;
    }

}
