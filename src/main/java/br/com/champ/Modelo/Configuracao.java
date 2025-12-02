package br.com.champ.Modelo;

import br.com.champ.Generico.ModeloGenerico;

/**
 *
 * @author andre
 */
public class Configuracao extends ModeloGenerico{
    
    private String caminhoApi;

    public String getCaminhoApi() {
        return caminhoApi;
    }

    public void setCaminhoApi(String caminhoApi) {
        this.caminhoApi = caminhoApi;
    }        
    
    
}
