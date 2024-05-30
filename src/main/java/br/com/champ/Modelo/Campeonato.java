package br.com.champ.Modelo;

import br.com.champ.Enums.Categoria;
import br.com.champ.Enums.StatusCamp;
import br.com.champ.Enums.TipoCampeonato;
import br.com.champ.Generico.ModeloGenerico;
import java.io.Serializable;
import java.util.List;

/**
 *
 * @author andre
 */
public class Campeonato extends ModeloGenerico implements Serializable {

    private String nome;
    private List<Team> teams;
    private List<Player> players;
    private StatusCamp status;
    private List<Partida> partidas;
    private List<Estatisticas> estatisticas;
    private String dataCamp;
    private String dataString;
    private Jogo jogo;
    private String dataFinal;
    private Anexo anexo;
    private TipoCampeonato tipoCampeonato;
    private Categoria categoria;
    private List<Grupo> grupos;
    private List<Fase> fasesCamp;    

    public List<Partida> getPartidas() {
        return partidas;
    }

    public void setPartidas(List<Partida> partidas) {
        this.partidas = partidas;
    }

    public List<Team> getTeams() {
        return teams;
    }

    public void setTeams(List<Team> teams) {
        this.teams = teams;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public StatusCamp getStatus() {
        return status;
    }

    public void setStatus(StatusCamp status) {
        this.status = status;
    }

    public List<Estatisticas> getEstatisticas() {
        return estatisticas;
    }

    public void setEstatisticas(List<Estatisticas> estatisticas) {
        this.estatisticas = estatisticas;
    }

    public String getDataCamp() {
        return dataCamp;
    }

    public void setDataCamp(String dataCamp) {
        this.dataCamp = dataCamp;
    }

    public String getDataString() {
        return dataString;
    }

    public void setDataString(String dataString) {
        this.dataString = dataString;
    }

    public Jogo getJogo() {
        return jogo;
    }

    public void setJogo(Jogo jogo) {
        this.jogo = jogo;
    }

    public String getDataFinal() {
        return dataFinal;
    }

    public void setDataFinal(String dataFinal) {
        this.dataFinal = dataFinal;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public void setPlayers(List<Player> players) {
        this.players = players;
    }

    public Anexo getAnexo() {
        return anexo;
    }

    public void setAnexo(Anexo anexo) {
        this.anexo = anexo;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }

    public TipoCampeonato getTipoCampeonato() {
        return tipoCampeonato;
    }

    public void setTipoCampeonato(TipoCampeonato tipoCampeonato) {
        this.tipoCampeonato = tipoCampeonato;
    }

    public List<Grupo> getGrupos() {
        return grupos;
    }

    public void setGrupos(List<Grupo> grupos) {
        this.grupos = grupos;
    }

    public List<Fase> getFasesCamp() {
        return fasesCamp;
    }

    public void setFasesCamp(List<Fase> fasesCamp) {
        this.fasesCamp = fasesCamp;
    }

}
