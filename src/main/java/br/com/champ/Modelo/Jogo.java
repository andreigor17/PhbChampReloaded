/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package br.com.champ.Modelo;

import br.com.champ.Generico.ModeloGenerico;
import java.util.List;

/**
 *
 * @author andre
 */
public class Jogo extends ModeloGenerico{
    
    private String nome;
    private Anexo anexo;
    private List<VersaoJogo> versoes;

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public Anexo getAnexo() {
        return anexo;
    }

    public void setAnexo(Anexo anexo) {
        this.anexo = anexo;
    }

    public List<VersaoJogo> getVersoes() {
        return versoes;
    }

    public void setVersoes(List<VersaoJogo> versoes) {
        this.versoes = versoes;
    }
    
    
    
}
