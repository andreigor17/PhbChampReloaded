/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.champ.Manager;

import br.com.champ.Enums.Url;
import br.com.champ.Modelo.Mapas;
import br.com.champ.Servico.AnexoServico;
import br.com.champ.Servico.MapaServico;
import br.com.champ.Utilitario.FacesUtil;
import br.com.champ.Utilitario.Mensagem;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.file.UploadedFile;

/**
 *
 * @author andre
 */
@ViewScoped
@Named
public class ManagerMapa implements Serializable {

    @EJB
    MapaServico mapaServico;
    @EJB
    AnexoServico anexoServico;
    private Mapas mapa;
    private String fileTemp;
    private List<Mapas> mapas;
    private UploadedFile file;
    private StreamedContent imagem;
    private String fotoMapa;

    @PostConstruct
    public void init() {

        String visualizarMapaId = FacesUtil
                .getRequestParameter("id");

        if (visualizarMapaId != null && !visualizarMapaId.isEmpty()) {
            this.mapa = this.mapaServico.pesquisarMapa(Long.parseLong(visualizarMapaId));
            if (this.mapa.getId() != null && this.mapa.getAnexo() != null) {
                this.fileTemp = this.mapa.getAnexo().getNome();
            }

        } else {
            instanciar();
        }

    }

    public void instanciar() {
        this.mapa = new Mapas();
        this.mapas = new ArrayList<>();
    }

    public void pesquisarMapas() {
        try {
            this.mapas = mapaServico.pesquisar();
        } catch (Exception ex) {
            System.err.println(ex);
        }
    }

    public Mapas getMapa() {
        return mapa;
    }

    public void setMapa(Mapas mapa) {
        this.mapa = mapa;
    }

    public List<Mapas> getMapas() {
        return mapas;
    }

    public void setMapas(List<Mapas> mapas) {
        this.mapas = mapas;
    }

    public UploadedFile getFile() {
        return file;
    }

    public void setFile(UploadedFile file) {
        this.file = file;
    }

    public StreamedContent getImagem() {
        return imagem;
    }

    public void setImagem(StreamedContent imagem) {
        this.imagem = imagem;
    }

    public String getFotoMapa() {
        return fotoMapa;
    }

    public void setFotoMapa(String fotoMapa) {
        this.fotoMapa = fotoMapa;
    }

    public void doUpload(FileUploadEvent event) {
        this.mapa.setAnexo(anexoServico.fileUploadTemp(event));
        this.fileTemp = this.mapa.getAnexo().getNome();

    }

    public void salvar() throws Exception {
        if (this.mapa.getAnexo() != null) {
            this.mapa.setAnexo(anexoServico.salvarAnexo(this.mapa.getAnexo()));
        }
        
        if (this.mapa.getId() != null) {

            try {
                this.mapa = mapaServico.save(this.mapa, this.mapa.getId(), Url.ATUALIZAR_MAPA.getNome());
            } catch (Exception ex) {
                System.err.println(ex);
            }
            Mensagem.successAndRedirect("Mapa atualizado com sucesso", "visualizarMapas.xhtml?id=" + this.mapa.getId());
        } else {
            try {
                this.mapa = mapaServico.save(this.mapa, null, Url.SALVAR_MAPA.getNome());
            } catch (Exception ex) {
                System.err.println(ex);
            }
            Mensagem.successAndRedirect("Mapa criado com sucesso", "visualizarMapas.xhtml?id=" + this.mapa.getId());
        }
    }

    public void visualizarMapa(Mapas mapa) {
        Mensagem.successAndRedirect("Operação realizada com sucesso!", "visualizarMapas.xhtml?id=" + mapa.getId());
    }

    public void excluir() {

    }

    public String getFileTemp() {
        return fileTemp;
    }

    public void setFileTemp(String fileTemp) {
        this.fileTemp = fileTemp;
    }

}
