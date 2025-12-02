/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.champ.Utilitario;

import br.com.champ.Modelo.ItemPartida;
import br.com.champ.Modelo.Partida;
import br.com.champ.Modelo.Player;
import br.com.champ.Modelo.Team;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author andre
 */
public class PartidaUtils {

    public static List<ItemPartida> gerarPartidasTimes(Partida p, Long id, Team time1, Team time2, int qtdItensPartidas) {

        int i = 0;
        List<ItemPartida> partidasGeradas = new ArrayList<>();
        try {
            for (i = 1; i <= qtdItensPartidas; i++) {
                ItemPartida newItem = new ItemPartida();
                if (id != null) {
                    newItem.setCamp(id);
                }

                newItem.setTeam1(time1);
                newItem.setTeam2(time2);
                partidasGeradas.add(newItem);
            }
        } catch (Exception ex) {
            System.err.println(ex);
        }
        return partidasGeradas;
    }

    public static List<ItemPartida> gerarPartidasPlayers(Partida p, Long id, Player player1, Player player2, int qtdItensPartidas) {

        int i = 0;
        List<ItemPartida> partidasGeradas = new ArrayList<>();
        try {
            for (i = 1; i <= qtdItensPartidas; i++) {
                ItemPartida newItem = new ItemPartida();
                if (id != null) {
                    newItem.setCamp(id);
                }

                newItem.setPlayer1(player1);
                newItem.setPlayer2(player2);
                partidasGeradas.add(newItem);
            }
        } catch (Exception ex) {
            System.err.println(ex);
        }
        return partidasGeradas;
    }

    public static String obterFaseAtual(int numTimesRestantes) {
        if (numTimesRestantes <= 2) {
            return "Final";
        } else if (numTimesRestantes <= 4) {
            return "Semifinais";
        } else if (numTimesRestantes <= 8) {
            return "Quartas de final";
        } else if (numTimesRestantes <= 16) {
            return "Oitavas de final";
        } else if (numTimesRestantes <= 32) {
            return "Fase inicial";
        } else {
            return "Fase desconhecida";
        }
    }

}
