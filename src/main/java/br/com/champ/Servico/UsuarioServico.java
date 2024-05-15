/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.champ.Servico;

import br.com.champ.Generico.ServicoGenerico;

import br.com.champ.Modelo.Usuario;
import jakarta.ejb.Stateless;

/**
 *
 * @author andre
 */

@Stateless
public class UsuarioServico extends ServicoGenerico<Usuario> {

    private Usuario usuario;

    public UsuarioServico() {
        super(Usuario.class);
    }

}
