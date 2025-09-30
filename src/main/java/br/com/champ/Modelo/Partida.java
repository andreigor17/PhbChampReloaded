/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.champ.Modelo;

import br.com.champ.Generico.ModeloGenerico;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 *
 * @author andre
 */
public class Partida extends ModeloGenerico implements Serializable {

    private List<ItemPartida> itemPartida;
    private Jogo jogo;
    private Team timeVencedor;
    private int contador;
    private int contadorEsperado;
    private boolean finalizada;
    private Long faseid;
    private String nome;
    @JsonFormat(pattern = "dd/MM/yyyy HH:mm") // garante que o JSON aceite esse formato
    private Date dataPartida;
    private LocalPartida local;
    private int minJogadores;
    private int maxJogadores;
    private List<Player> players;
    private List<Team> teams;
    private Player usuarioCadastro;
    private BigDecimal valor;

    public Partida() {
    }

    public List<ItemPartida> getItemPartida() {
        return itemPartida;
    }

    public void setItemPartida(List<ItemPartida> itemPartida) {
        this.itemPartida = itemPartida;
    }

    public Jogo getJogo() {
        return jogo;
    }

    public void setJogo(Jogo jogo) {
        this.jogo = jogo;
    }

    public Team getTimeVencedor() {
        return timeVencedor;
    }

    public void setTimeVencedor(Team timeVencedor) {
        this.timeVencedor = timeVencedor;
    }

    public int getContador() {
        return contador;
    }

    public void setContador(int contador) {
        this.contador = contador;
    }

    public int getContadorEsperado() {
        return contadorEsperado;
    }

    public void setContadorEsperado(int contadorEsperado) {
        this.contadorEsperado = contadorEsperado;
    }

    public boolean isFinalizada() {
        return finalizada;
    }

    public void setFinalizada(boolean finalizada) {
        this.finalizada = finalizada;
    }

    public Long getFaseid() {
        return faseid;
    }

    public void setFaseid(Long faseid) {
        this.faseid = faseid;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public Date getDataPartida() {
        return dataPartida;
    }

    public void setDataPartida(Date dataPartida) {
        this.dataPartida = dataPartida;
    }

    public LocalPartida getLocal() {
        return local;
    }

    public void setLocal(LocalPartida local) {
        this.local = local;
    }

    public int getMinJogadores() {
        return minJogadores;
    }

    public void setMinJogadores(int minJogadores) {
        this.minJogadores = minJogadores;
    }

    public int getMaxJogadores() {
        return maxJogadores;
    }

    public void setMaxJogadores(int maxJogadores) {
        this.maxJogadores = maxJogadores;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public void setPlayers(List<Player> players) {
        this.players = players;
    }

    public List<Team> getTeams() {
        return teams;
    }

    public void setTeams(List<Team> teams) {
        this.teams = teams;
    }

    public Player getUsuarioCadastro() {
        return usuarioCadastro;
    }

    public void setUsuarioCadastro(Player usuarioCadastro) {
        this.usuarioCadastro = usuarioCadastro;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }

}
