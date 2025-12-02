/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.champ.Generico;

import jakarta.persistence.MappedSuperclass;
import java.io.Serializable;


/**
 *
 * @author andre
 */
@MappedSuperclass
public abstract class ModeloGenerico implements Serializable {

    public Long id;

    /**
     * Quando false, o objeto encontra-se excluido/cancelado. Caso contrário
     * encontra-se ativo
     */
    private Boolean active = true;
    /**
     * Controle de versionamento da entidade, faz o controle de alterações
     * concorrentes
     */
    private int versao;
    private String dataCriacao;
    private String dataAtualizacao;

    public ModeloGenerico() {
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public int getVersao() {
        return versao;
    }

    public void setVersao(int versao) {
        this.versao = versao;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDataCriacao() {
        return dataCriacao;
    }

    public void setDataCriacao(String dataCriacao) {
        this.dataCriacao = dataCriacao;
    }

    public String getDataAtualizacao() {
        return dataAtualizacao;
    }

    public void setDataAtualizacao(String dataAtualizacao) {
        this.dataAtualizacao = dataAtualizacao;
    }

}
