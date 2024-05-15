/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.champ.Modelo;

import br.com.champ.Enums.Funcoes;
import br.com.champ.Generico.ModeloGenerico;
import java.io.Serializable;
import java.util.List;

/**
 *
 * @author andre
 */
public class Player extends ModeloGenerico implements Serializable {

    private String nome;
    private String sobreNome;
    private String nick;
    private boolean possuiTime = false;
    private boolean capitao;    
    private Funcoes funcao;
    private List<Estatisticas> estatisticas;
    private String urlSteam;
    private String steamID;   
    private List<Jogo> jogos;
    private Anexo anexo;

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getNick() {
        return nick;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }

    public Boolean getPossuiTime() {
        return possuiTime;
    }

    public void setPossuiTime(Boolean possuiTime) {
        this.possuiTime = possuiTime;
    }

    public Boolean getCapitao() {
        return capitao;
    }

    public void setCapitao(Boolean capitao) {
        this.capitao = capitao;
    }

    public String getSobreNome() {
        return sobreNome;
    }

    public void setSobreNome(String sobreNome) {
        this.sobreNome = sobreNome;
    }

    public boolean isPossuiTime() {
        return possuiTime;
    }

    public void setPossuiTime(boolean possuiTime) {
        this.possuiTime = possuiTime;
    }

    public boolean isCapitao() {
        return capitao;
    }

    public void setCapitao(boolean capitao) {
        this.capitao = capitao;
    }

    public Funcoes getFuncao() {
        return funcao;
    }

    public void setFuncao(Funcoes funcao) {
        this.funcao = funcao;
    }

    public List<Estatisticas> getEstatisticas() {
        return estatisticas;
    }

    public void setEstatisticas(List<Estatisticas> estatisticas) {
        this.estatisticas = estatisticas;
    }        

    public String getUrlSteam() {
        return urlSteam;
    }

    public void setUrlSteam(String urlSteam) {
        this.urlSteam = urlSteam;
    }

    public String getSteamID() {
        return steamID;
    }

    public void setSteamID(String steamID) {
        this.steamID = steamID;
    }

    public List<Jogo> getJogos() {
        return jogos;
    }

    public void setJogos(List<Jogo> jogos) {
        this.jogos = jogos;
    }

    public Anexo getAnexo() {
        return anexo;
    }

    public void setAnexo(Anexo anexo) {
        this.anexo = anexo;
    }

    @Override
    public String toString() {
        return "nome " + id;
    }
    
    
    
    
}
