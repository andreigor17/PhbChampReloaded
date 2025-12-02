/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package br.com.champ.Utilitario;

import br.com.champ.Modelo.Estatisticas;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author andre
 */
public class EstatisticasUtils {

    public static List<Estatisticas> calcularEstsGerais(List<Estatisticas> ests) {
        List<Estatisticas> somaGeral = new ArrayList<>();
        Integer kills = 0;
        Integer deaths = 0;
        Integer assists = 0;
        Integer roundsGanhos = 0;
        Integer roundsPerdidos = 0;
        Integer partidas = 0;
        Integer pontos = 0;
        for (Estatisticas e : ests) {
            kills = e.getKills();
            deaths = e.getDeaths();
            assists = e.getAssists();
            somaGeral.add(e);
        }

        return somaGeral;
    }

}
