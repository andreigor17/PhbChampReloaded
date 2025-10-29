package br.com.champ.Modelo;

import br.com.champ.Enums.Categoria;
import br.com.champ.Enums.StatusCamp;
import br.com.champ.Enums.TipoCampeonato;
import br.com.champ.Generico.ModeloGenerico;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 *
 * @author andre
 */
@JsonIgnoreProperties(ignoreUnknown = true)
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

    private List<Partida> rodadaSuica00;
    private List<Partida> rodadaSuica10;
    private List<Partida> rodadaSuica01;
    private List<Partida> rodadaSuica20;
    private List<Partida> rodadaSuica11;
    private List<Partida> rodadaSuica02;
    private List<Partida> rodadaSuica30;
    private List<Partida> rodadaSuica21;
    private List<Partida> rodadaSuica12;
    private List<Partida> rodadaSuica03;
    private List<Partida> rodadaSuica31;
    private List<Partida> rodadaSuica22;
    private List<Partida> rodadaSuica13;
    private List<Partida> rodadaSuica32;
    private List<Partida> rodadaSuica23;

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

    public List<Partida> getRodadaSuica00() {
        return rodadaSuica00;
    }

    public void setRodadaSuica00(List<Partida> rodadaSuica00) {
        this.rodadaSuica00 = rodadaSuica00;
    }

    public List<Partida> getRodadaSuica10() {
        return rodadaSuica10;
    }

    public void setRodadaSuica10(List<Partida> rodadaSuica10) {
        this.rodadaSuica10 = rodadaSuica10;
    }

    public List<Partida> getRodadaSuica01() {
        return rodadaSuica01;
    }

    public void setRodadaSuica01(List<Partida> rodadaSuica01) {
        this.rodadaSuica01 = rodadaSuica01;
    }

    public List<Partida> getRodadaSuica20() {
        return rodadaSuica20;
    }

    public void setRodadaSuica20(List<Partida> rodadaSuica20) {
        this.rodadaSuica20 = rodadaSuica20;
    }

    public List<Partida> getRodadaSuica11() {
        return rodadaSuica11;
    }

    public void setRodadaSuica11(List<Partida> rodadaSuica11) {
        this.rodadaSuica11 = rodadaSuica11;
    }

    public List<Partida> getRodadaSuica02() {
        return rodadaSuica02;
    }

    public void setRodadaSuica02(List<Partida> rodadaSuica02) {
        this.rodadaSuica02 = rodadaSuica02;
    }

    public List<Partida> getRodadaSuica30() {
        return rodadaSuica30;
    }

    public void setRodadaSuica30(List<Partida> rodadaSuica30) {
        this.rodadaSuica30 = rodadaSuica30;
    }

    public List<Partida> getRodadaSuica21() {
        return rodadaSuica21;
    }

    public void setRodadaSuica21(List<Partida> rodadaSuica21) {
        this.rodadaSuica21 = rodadaSuica21;
    }

    public List<Partida> getRodadaSuica12() {
        return rodadaSuica12;
    }

    public void setRodadaSuica12(List<Partida> rodadaSuica12) {
        this.rodadaSuica12 = rodadaSuica12;
    }

    public List<Partida> getRodadaSuica03() {
        return rodadaSuica03;
    }

    public void setRodadaSuica03(List<Partida> rodadaSuica03) {
        this.rodadaSuica03 = rodadaSuica03;
    }

    public List<Partida> getRodadaSuica31() {
        return rodadaSuica31;
    }

    public void setRodadaSuica31(List<Partida> rodadaSuica31) {
        this.rodadaSuica31 = rodadaSuica31;
    }

    public List<Partida> getRodadaSuica22() {
        return rodadaSuica22;
    }

    public void setRodadaSuica22(List<Partida> rodadaSuica22) {
        this.rodadaSuica22 = rodadaSuica22;
    }

    public List<Partida> getRodadaSuica13() {
        return rodadaSuica13;
    }

    public void setRodadaSuica13(List<Partida> rodadaSuica13) {
        this.rodadaSuica13 = rodadaSuica13;
    }

    public List<Partida> getRodadaSuica32() {
        return rodadaSuica32;
    }

    public void setRodadaSuica32(List<Partida> rodadaSuica32) {
        this.rodadaSuica32 = rodadaSuica32;
    }

    public List<Partida> getRodadaSuica23() {
        return rodadaSuica23;
    }

    public void setRodadaSuica23(List<Partida> rodadaSuica23) {
        this.rodadaSuica23 = rodadaSuica23;
    }

}
