/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.champ.Modelo;

import br.com.champ.Enums.Funcoes;
import br.com.champ.Generico.ModeloGenerico;
import java.io.Serializable;
import java.util.Date;
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
    private String login;
    private String senha;
    private boolean adminastror;
    private String email;
    private String steamId64;
    private Date dataNascimento;
    private String posicao;
    private Double rating;

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

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public boolean isAdminastror() {
        return adminastror;
    }

    public void setAdminastror(boolean adminastror) {
        this.adminastror = adminastror;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSteamId64() {
        return steamId64;
    }

    public void setSteamId64(String steamId64) {
        this.steamId64 = steamId64;
    }

    public Date getDataNascimento() {
        return dataNascimento;
    }

    public void setDataNascimento(Date dataNascimento) {
        this.dataNascimento = dataNascimento;
    }

    public String descPlayer() {
        return nome + " '" + nick + "' " + sobreNome;
    }

    public String getPosicao() {
        return posicao;
    }

    public void setPosicao(String posicao) {
        this.posicao = posicao;
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

}
