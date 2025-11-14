/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.champ.Modelo;

import br.com.champ.Generico.ModeloGenerico;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author andre
 */
public class Team extends ModeloGenerico implements Serializable {

    private String nome;
    private List<Player> players;
    private List<Estatisticas> estatisticas;
    private Anexo anexo;
    private boolean timeAmistoso;
    private String sigla;
    private Player capitao;
    private List<Jogo> jogos;

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public void setPlayers(List<Player> players) {
        this.players = players;
    }

    public List<Estatisticas> getEstatisticas() {
        return estatisticas;
    }

    public void setEstatisticas(List<Estatisticas> estatisticas) {
        this.estatisticas = estatisticas;
    }

    public Anexo getAnexo() {
        return anexo;
    }

    public void setAnexo(Anexo anexo) {
        this.anexo = anexo;
    }

    public boolean isTimeAmistoso() {
        return timeAmistoso;
    }

    public void setTimeAmistoso(boolean timeAmistoso) {
        this.timeAmistoso = timeAmistoso;
    }

    public String getSigla() {
        return sigla;
    }

    public void setSigla(String sigla) {
        this.sigla = sigla;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Team other = (Team) obj;
        if (this.timeAmistoso != other.timeAmistoso) {
            return false;
        }
        if (!Objects.equals(this.nome, other.nome)) {
            return false;
        }
        if (!Objects.equals(this.sigla, other.sigla)) {
            return false;
        }
        if (!Objects.equals(this.players, other.players)) {
            return false;
        }
        if (!Objects.equals(this.estatisticas, other.estatisticas)) {
            return false;
        }
        return Objects.equals(this.anexo, other.anexo);
    }

    public List<Jogo> getJogos() {
        return jogos;
    }

    public void setJogos(List<Jogo> jogos) {
        this.jogos = jogos;
    }

    public Player getCapitao() {
        return capitao;
    }

    public void setCapitao(Player capitao) {
        this.capitao = capitao;
    }

}
