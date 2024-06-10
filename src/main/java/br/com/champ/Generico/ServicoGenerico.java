/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.champ.Generico;

/**
 *
 * @author andre
 */
public class ServicoGenerico<T> {

    private Class<T> entity;

    public ServicoGenerico(Class<T> entity) {
        this.entity = entity;

    }
}
