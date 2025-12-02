package br.com.champ.Modelo;

import br.com.champ.Generico.ModeloGenerico;
import java.io.Serializable;

/**
 *
 * @author andre
 */
public class Anexo extends ModeloGenerico implements Serializable {

    private byte[] imagem;
    private String nome;
    private String nomeExibicao;
    private String dataArquivo;

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getNomeExibicao() {
        return nomeExibicao;
    }

    public void setNomeExibicao(String nomeExibicao) {
        this.nomeExibicao = nomeExibicao;
    }

    public String getDataArquivo() {
        return dataArquivo;
    }

    public void setDataArquivo(String dataArquivo) {
        this.dataArquivo = dataArquivo;
    }

    public byte[] getImagem() {
        return imagem;
    }

    public void setImagem(byte[] imagem) {
        this.imagem = imagem;
    }

}
