/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package br.com.champ.Servico;

import jakarta.enterprise.context.ApplicationScoped;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author andrepc
 */
@ApplicationScoped
public class RconService {

    private static final String RCON_PATH = "/opt/rcon-0.10.3-amd64_linux/rcon";
    private static final String RCON_ADDRESS = "0.0.0.0:27015";
    private static final String RCON_PASSWORD = "fxiladmin";

    /**
     * Executa um comando RCON e retorna a resposta
     */
    public String executeCommand(String command) {
        if (command == null || command.trim().isEmpty()) {
            return "Comando vazio";
        }

        try {
            // Monta o comando completo
            List<String> comandos = new ArrayList<>();
            comandos.add(RCON_PATH);
            comandos.add("-a");
            comandos.add(RCON_ADDRESS);
            comandos.add("-p");
            comandos.add(RCON_PASSWORD);
            comandos.add(command);

            // Executa o processo
            ProcessBuilder pb = new ProcessBuilder(comandos);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            // Lê a saída
            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }

            // Aguarda conclusão
            int exitCode = process.waitFor();

            if (exitCode != 0) {
                return "Erro ao executar comando (código: " + exitCode + ")";
            }

            String result = output.toString().trim();
            return result.isEmpty() ? "Comando executado (sem retorno)" : result;

        } catch (Exception e) {
            return "ERRO: " + e.getMessage();
        }
    }

    /**
     * Testa a conexão RCON
     */
    public boolean testarConexao() {
        String resultado = executeCommand("status");
        return resultado != null && !resultado.startsWith("ERRO");
    }

}
