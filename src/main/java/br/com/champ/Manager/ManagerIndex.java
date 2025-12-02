package br.com.champ.Manager;

import br.com.champ.Modelo.Campeonato;
import br.com.champ.Modelo.Estatisticas;
import br.com.champ.Modelo.Partida;
import br.com.champ.Modelo.Player;
import br.com.champ.Modelo.Team;
import br.com.champ.Servico.CampeonatoServico;
import br.com.champ.Servico.EstatisticaServico;
import br.com.champ.Servico.PartidaServico;
import br.com.champ.Servico.PlayerServico;
import br.com.champ.Servico.TeamServico;
import br.com.champ.Utilitario.Utils;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author andre
 */
@ViewScoped
@Named
public class ManagerIndex implements Serializable {

    @EJB
    CampeonatoServico campServico;
    @EJB
    EstatisticaServico estatisticaServico;
    @EJB
    PartidaServico partidaServico;
    @EJB
    CampeonatoServico campeonatoServico;
    @EJB
    PlayerServico playerServico;
    @EJB
    TeamServico teamServico;

    private Campeonato camp;
    private List<Campeonato> campsAtuais;
    private List<Team> times;
    private List<Estatisticas> estatisticasTime;
    private List<Estatisticas> ests;
    private List<Partida> partidas;
    private List<Campeonato> camps;
    private List<Campeonato> campsCards;
    private List<Player> playersAtivos;
    private List<Player> playersOfTheWeek;
    private List<String> playsOfTheWeek;

    @PostConstruct
    public void init() {
        try {
            instanciar();
            
            // Inicializações com verificação de null
            try {
                this.partidas = partidaServico.pesquisarPartidasGeral();
                if (this.partidas == null) {
                    this.partidas = new ArrayList<>();
                }
            } catch (Exception e) {
                this.partidas = new ArrayList<>();
                Logger.getLogger(ManagerIndex.class.getName()).log(Level.WARNING, "Erro ao buscar partidas: " + e.getMessage());
            }
            
            try {
                this.camps = campeonatoServico.pesquisarIndex(null);
                if (this.camps == null) {
                    this.camps = new ArrayList<>();
                }
            } catch (Exception e) {
                this.camps = new ArrayList<>();
                Logger.getLogger(ManagerIndex.class.getName()).log(Level.WARNING, "Erro ao buscar campeonatos: " + e.getMessage());
            }
            
            try {
                this.campsAtuais = campeonatoServico.pesquisarIndex(3);
                if (this.campsAtuais == null) {
                    this.campsAtuais = new ArrayList<>();
                }
            } catch (Exception e) {
                this.campsAtuais = new ArrayList<>();
                Logger.getLogger(ManagerIndex.class.getName()).log(Level.WARNING, "Erro ao buscar campeonatos atuais: " + e.getMessage());
            }
            
            try {
                this.playersAtivos = playerServico.buscaPlayers();
                if (this.playersAtivos == null) {
                    this.playersAtivos = new ArrayList<>();
                }
            } catch (Exception e) {
                this.playersAtivos = new ArrayList<>();
                Logger.getLogger(ManagerIndex.class.getName()).log(Level.WARNING, "Erro ao buscar players: " + e.getMessage());
            }
            
            try {
                this.times = teamServico.buscaTimes();
                if (this.times == null) {
                    this.times = new ArrayList<>();
                }
            } catch (Exception e) {
                this.times = new ArrayList<>();
                Logger.getLogger(ManagerIndex.class.getName()).log(Level.WARNING, "Erro ao buscar times: " + e.getMessage());
            }

            // Inicializações seguras para novos blocos (sem dependência externa):
            // Players of the Week: seleciona até 3 jogadores ativos (se disponíveis)
            if (this.playersAtivos != null && !this.playersAtivos.isEmpty()) {
                int limit = Math.min(3, this.playersAtivos.size());
                this.playersOfTheWeek = new ArrayList<>(this.playersAtivos.subList(0, limit));
            } else {
                this.playersOfTheWeek = new ArrayList<>();
            }

            // Jogadas da Week: cria destaques textuais simples baseados na quantidade de partidas
            this.playsOfTheWeek = new ArrayList<>();
            if (this.partidas != null && !this.partidas.isEmpty()) {
                int max = Math.min(3, this.partidas.size());
                for (int i = 0; i < max; i++) {
                    this.playsOfTheWeek.add("Destaque da partida #" + (i + 1));
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(ManagerIndex.class.getName()).log(Level.SEVERE, null, ex);
            // Garantir que listas não sejam null em caso de erro
            instanciar();
        }

    }

    public void instanciar() {
        this.camp = new Campeonato();
        this.campsAtuais = new ArrayList<>();
        this.campsCards = new ArrayList<>();
        this.times = new ArrayList<>();
        this.estatisticasTime = new ArrayList<>();
        this.ests = new ArrayList<>();
        this.partidas = new ArrayList<>();
        this.playersAtivos = new ArrayList<>();
        this.playersOfTheWeek = new ArrayList<>();
        this.playsOfTheWeek = new ArrayList<>();

    }

    public void limpar() {
        instanciar();
    }

    public List<Campeonato> getCampsCards() {
        return campsCards;
    }

    public void setCampsCards(List<Campeonato> campsCards) {
        this.campsCards = campsCards;
    }

    public Campeonato getCamp() {
        return camp;
    }

    public void setCamp(Campeonato camp) {
        this.camp = camp;
    }

    public List<Campeonato> getCampsAtuais() {
        return campsAtuais;
    }

    public void setCampsAtuais(List<Campeonato> campsAtuais) {
        this.campsAtuais = campsAtuais;
    }

    public List<Team> getTimes() {
        return times;
    }

    public void setTimes(List<Team> times) {
        this.times = times;
    }

    public List<Campeonato> autoCompletarCamps() {
        return campServico.autoCompleteCamps();
    }

    public List<Estatisticas> getEstatisticasTime() {
        return estatisticasTime;
    }

    public void setEstatisticasTime(List<Estatisticas> estatisticasTime) {
        this.estatisticasTime = estatisticasTime;
    }

    public List<Estatisticas> getEsts() {
        return ests;
    }

    public void setEsts(List<Estatisticas> ests) {
        this.ests = ests;
    }

    public List<Campeonato> getCamps() {
        return camps;
    }

    public void setCamps(List<Campeonato> camps) {
        this.camps = camps;
    }

    public List<Partida> getPartidas() {
        return partidas;
    }

    public void setPartidas(List<Partida> partidas) {
        this.partidas = partidas;
    }

    public String nomeCamp(Long idCamp) {
        if (idCamp != null) {
            Campeonato camp = campeonatoServico.buscaCamp(idCamp);
            if (Utils.isNotEmpty(camp)) {
                String nome = camp.getNome();
                return nome;
            }
        }
        return null;
    }

    public List<Player> getPlayersAtivos() {
        return playersAtivos;
    }

    public void setPlayersAtivos(List<Player> playersAtivos) {
        this.playersAtivos = playersAtivos;
    }

    public List<Player> getPlayersOfTheWeek() {
        return playersOfTheWeek;
    }

    public void setPlayersOfTheWeek(List<Player> playersOfTheWeek) {
        this.playersOfTheWeek = playersOfTheWeek;
    }

    public List<String> getPlaysOfTheWeek() {
        return playsOfTheWeek;
    }

    public void setPlaysOfTheWeek(List<String> playsOfTheWeek) {
        this.playsOfTheWeek = playsOfTheWeek;
    }

}
