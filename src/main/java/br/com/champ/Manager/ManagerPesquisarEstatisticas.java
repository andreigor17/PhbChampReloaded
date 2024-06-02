/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.champ.Manager;

import br.com.champ.Enums.Url;
import br.com.champ.Modelo.Estatisticas;
import br.com.champ.Modelo.ItemPartida;
import br.com.champ.Servico.EstatisticaServico;
import br.com.champ.Servico.ItemPartidaServico;
import br.com.champ.Utilitario.FacesUtil;
import br.com.champ.Utilitario.Mensagem;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author andre
 */
@ViewScoped
@Named
public class ManagerPesquisarEstatisticas implements Serializable {

    @EJB
    private EstatisticaServico estatisticasServico;
    @EJB
    private ItemPartidaServico itemPartidaServico;
    private Estatisticas estatisticas;
    private ItemPartida itemPartida;
    private List<Estatisticas> ests;
    private List<Estatisticas> estsTime1Visualizar;
    private List<Estatisticas> estsTime2Visualizar;
    private List<Estatisticas> estsGerais;

    @PostConstruct
    public void init() {
        instanciar();
        String visualizarItemId = FacesUtil
                .getRequestParameter("id");
        if (visualizarItemId != null && !visualizarItemId.isEmpty()) {
            this.itemPartida = this.itemPartidaServico.buscaItem(Long.parseLong(visualizarItemId));
            if (this.itemPartida.getTeam1() != null && this.itemPartida.getTeam2() != null) {
                this.estsTime1Visualizar = estatisticasServico.estatisticaPorItemPartidaTeam(this.itemPartida.getTeam1().getId(), this.itemPartida.getId());
                this.estsTime2Visualizar = estatisticasServico.estatisticaPorItemPartidaTeam(this.itemPartida.getTeam2().getId(), this.itemPartida.getId());
            } else {
                this.estsTime1Visualizar = estatisticasServico.estatisticaPorItemPartidaPlayer(this.itemPartida.getPlayer1().getId(), this.itemPartida.getId());
                this.estsTime2Visualizar = estatisticasServico.estatisticaPorItemPartidaPlayer(this.itemPartida.getPlayer2().getId(), this.itemPartida.getId());
            }

        }

    }

    public void instanciar() {
        this.itemPartida = new ItemPartida();
        this.estatisticas = new Estatisticas();
        this.ests = new ArrayList<>();
        this.estsTime1Visualizar = new ArrayList<>();
        this.estsTime2Visualizar = new ArrayList<>();
        this.estsGerais = new ArrayList<>();
    }

    public Estatisticas getEstatisticas() {
        return estatisticas;
    }

    public void setEstatisticas(Estatisticas estatisticas) {
        this.estatisticas = estatisticas;
    }

    public ItemPartida getItemPartida() {
        return itemPartida;
    }

    public void setItemPartida(ItemPartida itemPartida) {
        this.itemPartida = itemPartida;
    }

    public List<Estatisticas> getEsts() {
        return ests;
    }

    public void setEsts(List<Estatisticas> ests) {
        this.ests = ests;
    }

    public List<Estatisticas> getEstsTime1Visualizar() {
        return estsTime1Visualizar;
    }

    public void setEstsTime1Visualizar(List<Estatisticas> estsTime1Visualizar) {
        this.estsTime1Visualizar = estsTime1Visualizar;
    }

    public List<Estatisticas> getEstsTime2Visualizar() {
        return estsTime2Visualizar;
    }

    public void setEstsTime2Visualizar(List<Estatisticas> estsTime2Visualizar) {
        this.estsTime2Visualizar = estsTime2Visualizar;
    }

    public void salvar() {

        if (this.itemPartida.getScoreT1() != null && this.itemPartida.getScoreT2() != null) {
            this.itemPartida.setJogado(true);

            for (Estatisticas e : estsTime2Visualizar) {
                e.setRoundsGanhos(this.itemPartida.getScoreT2());
                e.setRoundsPerdidos(this.itemPartida.getScoreT1());
                break;
            }

            for (Estatisticas e : estsTime1Visualizar) {
                e.setRoundsGanhos(this.itemPartida.getScoreT1());
                e.setRoundsPerdidos(this.itemPartida.getScoreT2());
                break;
            }

            if (this.itemPartida.getScoreT1() > this.itemPartida.getScoreT2()) {
                this.itemPartida.setTimeVencedor(this.itemPartida.getTeam1());
            } else {
                this.itemPartida.setTimeVencedor(this.itemPartida.getTeam2());
            }
            try {
                itemPartidaServico.salvar(this.itemPartida, this.itemPartida.getId(), Url.ATUALIZAR_ITEM_PARTIDA.getNome());
            } catch (Exception ex) {
                System.err.println(ex);
            }
        } else {
            Mensagem.error("Partida deve ter um placar!");
            return;
        }
        this.estsGerais.addAll(estsTime1Visualizar);
        this.estsGerais.addAll(estsTime2Visualizar);
        for (Estatisticas e : this.estsGerais) {
            try {
                estatisticasServico.salvar(e, e.getId(), Url.ATUALIZAR_ESTATISTICA.getNome());
            } catch (Exception ex) {
                System.err.println(ex);
            }
        }
        Mensagem.successAndRedirect("Dados atualizados com sucesso!", "visualizarPartida.xhtml?id=" + this.itemPartida.getPartida());
    }

    public void cancelar() {
        Mensagem.successAndRedirect("Operação cancelada com sucesso!", "visualizarPartida.xhtml?id=" + this.itemPartida.getPartida());
    }

}
