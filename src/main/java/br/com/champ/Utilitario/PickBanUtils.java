/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package br.com.champ.Utilitario;

import br.com.champ.Enums.TipoPickBan;
import br.com.champ.Modelo.ItemPartida;
import br.com.champ.Modelo.Mapas;
import br.com.champ.Modelo.Partida;
import br.com.champ.Modelo.Team;
import br.com.champ.vo.PickBanVo;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author andre
 */
public class PickBanUtils {

    public static List<PickBanVo> gerarListaPB(Team team1, Team team2, int md, Team timeInicial) {
        List<PickBanVo> pickBanVo = new ArrayList<>();

        Team primeiro = timeInicial;
        Team segundo = (timeInicial.equals(team1)) ? team2 : team1;

        if (md == 1) {

            pickBanVo.add(new PickBanVo(primeiro, TipoPickBan.BAN));
            pickBanVo.add(new PickBanVo(segundo, TipoPickBan.BAN));
            pickBanVo.add(new PickBanVo(primeiro, TipoPickBan.BAN));
            pickBanVo.add(new PickBanVo(segundo, TipoPickBan.BAN));
            pickBanVo.add(new PickBanVo(primeiro, TipoPickBan.BAN));
            pickBanVo.add(new PickBanVo(segundo, TipoPickBan.BAN));
            pickBanVo.add(new PickBanVo(primeiro, TipoPickBan.PICK));
        } else if (md == 2) {

            pickBanVo.add(new PickBanVo(primeiro, TipoPickBan.BAN));
            pickBanVo.add(new PickBanVo(segundo, TipoPickBan.BAN));
            pickBanVo.add(new PickBanVo(primeiro, TipoPickBan.PICK));
            pickBanVo.add(new PickBanVo(segundo, TipoPickBan.PICK));
        } else if (md == 3) {

            pickBanVo.add(new PickBanVo(primeiro, TipoPickBan.BAN));
            pickBanVo.add(new PickBanVo(segundo, TipoPickBan.BAN));
            pickBanVo.add(new PickBanVo(primeiro, TipoPickBan.PICK));
            pickBanVo.add(new PickBanVo(segundo, TipoPickBan.PICK));
            pickBanVo.add(new PickBanVo(primeiro, TipoPickBan.BAN));
            pickBanVo.add(new PickBanVo(segundo, TipoPickBan.BAN));
            pickBanVo.add(new PickBanVo(primeiro, TipoPickBan.PICK));
        } else if (md == 5) {

            pickBanVo.add(new PickBanVo(primeiro, TipoPickBan.BAN));
            pickBanVo.add(new PickBanVo(segundo, TipoPickBan.BAN));
            pickBanVo.add(new PickBanVo(primeiro, TipoPickBan.BAN));
            pickBanVo.add(new PickBanVo(segundo, TipoPickBan.BAN));
            pickBanVo.add(new PickBanVo(primeiro, TipoPickBan.PICK));
            pickBanVo.add(new PickBanVo(segundo, TipoPickBan.PICK));
            pickBanVo.add(new PickBanVo(primeiro, TipoPickBan.PICK));
        } else {
            return null;
        }

        return pickBanVo;
    }

    public static List<ItemPartida> setarMapas(List<Mapas> mapasSelecionados, List<ItemPartida> itens) {
        if (mapasSelecionados.size() == 1) {
            itens.get(0).setMapas(mapasSelecionados.get(0));
            return itens;
        } else if (mapasSelecionados.size() == 2) {
            itens.get(0).setMapas(mapasSelecionados.get(0));
            itens.get(1).setMapas(mapasSelecionados.get(1));
            return itens;
        } else if (mapasSelecionados.size() == 3) {
            itens.get(0).setMapas(mapasSelecionados.get(0));
            itens.get(1).setMapas(mapasSelecionados.get(1));
            itens.get(2).setMapas(mapasSelecionados.get(2));
            return itens;
        } else if (mapasSelecionados.size() == 5) {
            itens.get(0).setMapas(mapasSelecionados.get(0));
            itens.get(1).setMapas(mapasSelecionados.get(1));
            itens.get(2).setMapas(mapasSelecionados.get(2));
            itens.get(3).setMapas(mapasSelecionados.get(3));
            itens.get(4).setMapas(mapasSelecionados.get(4));
            return itens;
        } else {
            return null;
        }
    }

}
