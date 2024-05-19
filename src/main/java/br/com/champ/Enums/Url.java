/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.champ.Enums;

/**
 *
 * @author andre
 */
public enum Url {

    SALVAR_PLAYER("/api/player"),
    ATUALIZAR_PLAYER("/api/player/"),
    BUSCAR_PLAYERS("/api/player/players"),
    BUSCAR_PLAYER_NOME("/api/player"),
    BUSCAR_PLAYER_ID("/api/player/"),
    APAGAR_PLAYER("/api/player/"),
    SALVAR_TIME("/api/team"),
    ATUALIZAR_TIME("/api/team/"),
    SALVAR_CAMPEONATO("/api/campeonato"),
    ATUALIZAR_CAMPEONATO("/api/campeonato/"),
    EXCLUIR_CAMPEONATO("/api/campeonato/excluir/"),
    BUSCAR_CAMPEONATO_ID("/api/campeonato/"),
    BUSCAR_CAMPEONATO_PLAYERS("/api/campeonato/"),
    BUSCAR_CAMPEONATOS("/api/campeonatos"),
    CAMPEONATOS_PLAYERS("/api/campeonato/camp_players/"),
    SALVAR_PARTIDA("/api/partida"),
    ATUALIZAR_PARTIDA("/api/partida/"),
    SALVAR_ESTATISTICA("/api/estatistica"),
    BUSCAR_PARTIDAS("/api/partida/partidas"),
    BUSCAR_PARTIDA_ID("/api/partida"),
    BUSCAR_PARTIDA_JOGO("/api/partida/partida-jogo"),
    BUSCAR_PARTIDA_CAMP("/api/partida/partida-camp/"),
    ATUALIZAR_ESTATISTICA("/api/estatistica/"),
    BUSCAR_ESTATISTICA_ID("/api/estatistica/"),
    ESTATISTICA_POR_TIME_CAMP("/api/estatisticasPorTimeCamp/"),
    ESTATISTICA_POR_PLAYER_CAMP("/api/estatistica/estatisticasPorPlayerCamp/"),
    ESTATISTICA_POR_ITEMPARTIDA_TIME("/api/estatistica/estatisticasPorItemPartidaTeam/"),
    ESTATISTICA_POR_ITEMPARTIDA_PLAYER("/api/estatistica/estatisticasPorItemPartidaPlayer/"),
    ESTATISTICAS_GERAIS_POR_PARTIDA_PLAYER("/api/estatistica/estatisticasPorPartidaPlayer/"),
    SALVAR_ITEM_PARTIDA("/api/item-partida"),
    ATUALIZAR_ITEM_PARTIDA("/api/item-partida/"),
    SALVAR_MAPA("/api/mapa"),
    ATUALIZAR_MAPA("/api/mapa/"),
    BUSCAR_MAPA_ID("/api/mapa/"),
    BUSCAR_MAPA_NOME("/api/mapa/"),
    BUSCAR_MAPAS("/api/mapa/mapas"),
    SALVAR_JOGO("/api/jogo"),
    ATUALIZAR_JOGO("/api/jogo/"),
    EXCLUIR_JOGO("/api/jogo/excluir/"),
    BUSCAR_JOGOS("/api/jogos"),
    BUSCAR_JOGO_NOME("/api/jogo/"),
    SALVAR_ANEXO("/api/anexo"),
    ATUALIZAR_ANEXO("/api/anexo"),
    EXCLUIR_ANEXO("/api/anexo/excluir/"),
    SALVAR_VERSAO("/versao"),
    ATUALIZAR_VERSAO("/versao"),
    EXCLUIR_VERSAO("/versao/excluir/"),
    LOGIN("/api/login/auth"),
    REGISTRAR_USUARIO("/api/login/usuario");
    

    private String nome;    

    public String getNome() {
        return nome;
    }


    private Url(String nome) {
        this.nome = nome;        
    }

}
