/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package br.com.champ.Manager;

import br.com.champ.Enums.Cs2ConsoleCommand;
import br.com.champ.Servico.RconService;
import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author andre
 */
@Named
@ViewScoped
public class ManagerRcon implements Serializable {

    @Inject
    private RconService rconService;

    private String comando;
    private List<String> historico;
    private int indiceHistorico = -1;
    private boolean conectado = false;

    @PostConstruct
    public void init() {
        historico = new ArrayList<>();
        testarConexao();
        adicionarLog("=== Console RCON CS2 ===");
        adicionarLog("Digite 'help' para ver comandos disponíveis");
        adicionarLog("---");
    }

    public void testarConexao() {
        conectado = rconService.testarConexao();
        if (conectado) {
            adicionarLog("[✓] Conectado ao servidor CS2");
        } else {
            adicionarLog("[✗] Erro ao conectar ao servidor");
        }
    }

    public void executarComando() {
        if (comando == null || comando.trim().isEmpty()) {
            return;
        }

        String cmd = comando.trim();

        // Adiciona comando ao histórico visual
        adicionarLog("> " + cmd);

        // Comandos especiais do console
        if (cmd.equalsIgnoreCase("clear") || cmd.equalsIgnoreCase("cls")) {
            historico.clear();
            adicionarLog("=== Console limpo ===");
            comando = "";
            return;
        }

        if (cmd.equalsIgnoreCase("help")) {
            mostrarAjuda();
            comando = "";
            return;
        }

        // Executa comando RCON
        String resultado = rconService.executeCommand(cmd);

        // Adiciona resultado ao histórico
        if (resultado != null && !resultado.isEmpty()) {
            for (String linha : resultado.split("\n")) {
                adicionarLog(linha);
            }
        }

        comando = "";
        indiceHistorico = -1;
    }

    private void mostrarAjuda() {
        adicionarLog("--- Comandos RCON Disponíveis ---");
        adicionarLog("status - Mostra status do servidor");
        adicionarLog("changelevel <mapa> - Muda o mapa (ex: de_dust2)");
        adicionarLog("mp_roundtime <minutos> - Tempo da rodada");
        adicionarLog("mp_restartgame <segundos> - Reinicia partida");
        adicionarLog("say <msg> - Mensagem no chat");
        adicionarLog("kick <nome> - Expulsa jogador");
        adicionarLog("sv_cheats <0/1> - Ativa/desativa cheats");
        adicionarLog("---");
        adicionarLog("clear - Limpa o console");
        adicionarLog("help - Mostra esta ajuda");
    }

    private void adicionarLog(String mensagem) {
        String timestamp = new SimpleDateFormat("HH:mm:ss").format(new Date());
        historico.add("[" + timestamp + "] " + mensagem);

        // Limita histórico a 200 linhas
        if (historico.size() > 200) {
            historico.remove(0);
        }
    }

    public String getHistoricoFormatado() {
        StringBuilder sb = new StringBuilder();
        for (String linha : historico) {
            sb.append(linha).append("\n");
        }
        return sb.toString();
    }

    // Getters e Setters
    public String getComando() {
        return comando;
    }

    public void setComando(String comando) {
        this.comando = comando;
    }

    public List<String> getHistorico() {
        return historico;
    }

    public boolean isConectado() {
        return conectado;
    }

}
