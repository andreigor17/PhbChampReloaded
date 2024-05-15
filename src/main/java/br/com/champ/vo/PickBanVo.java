/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package br.com.champ.vo;

import br.com.champ.Enums.TipoPickBan;
import br.com.champ.Modelo.Mapas;
import br.com.champ.Modelo.Team;

/**
 *
 * @author andre
 */
public class PickBanVo {

    private Team team;
    private TipoPickBan tipoPickBan;
    private Mapas mapas;

    public PickBanVo(Team team, TipoPickBan tipoPickBan) {
        this.team = team;
        this.tipoPickBan = tipoPickBan;
    }

    public Team getTeam() {
        return team;
    }

    public void setTeam(Team team) {
        this.team = team;
    }

    public TipoPickBan getTipoPickBan() {
        return tipoPickBan;
    }

    public void setTipoPickBan(TipoPickBan tipoPickBan) {
        this.tipoPickBan = tipoPickBan;
    }

    public Mapas getMapas() {
        return mapas;
    }

    public void setMapas(Mapas mapas) {
        this.mapas = mapas;
    }

}
