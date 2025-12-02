/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.champ.Utilitario;

import br.com.champ.Enums.Categoria;
import br.com.champ.Enums.Cs2ConsoleCommand;
import br.com.champ.Enums.Funcoes;
import br.com.champ.Enums.Game;
import br.com.champ.Enums.StatusCamp;
import br.com.champ.Enums.TipoCampeonato;
import jakarta.faces.model.SelectItem;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author andre
 */
@ViewScoped
@Named
public class Enums implements Serializable {

    public List<SelectItem> funcoes() {
        List<SelectItem> itens = new ArrayList<SelectItem>();
        for (Funcoes funcao : Funcoes.values()) {
            itens.add(new SelectItem(funcao, funcao.getNome()));
        }
        return itens;
    }

    public List<SelectItem> statusCamp() {
        List<SelectItem> itens = new ArrayList<SelectItem>();
        for (StatusCamp status : StatusCamp.values()) {
            itens.add(new SelectItem(status, status.getNome()));
        }
        return itens;
    }

    public List<SelectItem> games() {
        List<SelectItem> itens = new ArrayList<SelectItem>();
        for (Game game : Game.values()) {
            itens.add(new SelectItem(game, game.getNome()));
        }
        return itens;
    }

    public List<SelectItem> categorias() {
        List<SelectItem> itens = new ArrayList<SelectItem>();
        for (Categoria categoria : Categoria.values()) {
            itens.add(new SelectItem(categoria, categoria.getNome()));
        }
        return itens;
    }

    public List<SelectItem> tipoCampeonato() {
        List<SelectItem> itens = new ArrayList<SelectItem>();
        for (TipoCampeonato tipo : TipoCampeonato.values()) {
            itens.add(new SelectItem(tipo, tipo.getNome()));
        }
        return itens;
    }

    public List<Cs2ConsoleCommand> sugerirComandos(String query) {
        return Arrays.stream(Cs2ConsoleCommand.values())
                .filter(cmd -> cmd.getComando().toLowerCase().contains(query.toLowerCase()))
                .collect(Collectors.toList());
    }
}
