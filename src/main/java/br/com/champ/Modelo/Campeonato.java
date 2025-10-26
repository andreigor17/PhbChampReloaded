package br.com.champ.Modelo;

import br.com.champ.Enums.Categoria;
import br.com.champ.Enums.StatusCamp;
import br.com.champ.Enums.TipoCampeonato;
import br.com.champ.Generico.ModeloGenerico;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.io.Serializable;
import java.util.Date;
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
    @JsonFormat(pattern = "dd/MM/yyyy HH:mm") // garante que o JSON aceite esse formato
    private Date dataCamp;
    @JsonFormat(pattern = "dd/MM/yyyy HH:mm") // garante que o JSON aceite esse formato
    private Date dataPrazoCamp;
    private Jogo jogo;
    private Anexo anexo;
    private TipoCampeonato tipoCampeonato;
    private Categoria categoria;
    private List<Grupo> grupos;
    private List<Fase> fasesCamp;
    private String observacao;
    private boolean gerarTimesPorSorteio;
    private Integer quantidadePorTime;

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

    public Date getDataCamp() {
        return dataCamp;
    }

    public void setDataCamp(Date dataCamp) {
        this.dataCamp = dataCamp;
    }

    public Date getDataPrazoCamp() {
        return dataPrazoCamp;
    }

    public void setDataPrazoCamp(Date dataPrazoCamp) {
        this.dataPrazoCamp = dataPrazoCamp;
    }

    public Jogo getJogo() {
        return jogo;
    }

    public void setJogo(Jogo jogo) {
        this.jogo = jogo;
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

    public String getObservacao() {
        return observacao;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }

    public boolean isGerarTimesPorSorteio() {
        return gerarTimesPorSorteio;
    }

    public void setGerarTimesPorSorteio(boolean gerarTimesPorSorteio) {
        this.gerarTimesPorSorteio = gerarTimesPorSorteio;
    }

    public Integer getQuantidadePorTime() {
        return quantidadePorTime;
    }

    public void setQuantidadePorTime(Integer quantidadePorTime) {
        this.quantidadePorTime = quantidadePorTime;
    }

}
