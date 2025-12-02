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
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.primefaces.PrimeFaces;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.file.UploadedFile;

/**
 *
 * @author andre
 */
@ViewScoped
@Named
public class ManagerMapa extends ManagerBase {

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
    private Mapas mapaDeletar;
    private Mapas mapaPesquisar;

    @PostConstruct
    public void init() {
        FacesContext context = FacesContext.getCurrentInstance();
        
        // Verifica se contexto já foi released ou response já foi committed
        if (context == null || context.getResponseComplete()) {
            return;
        }
        
        try {
            // Se não estiver logado ou não for admin, redireciona
            if (!isUsuarioLogado() || !isAdmin()) {
                // Verifica se response não foi committed antes de redirecionar
                if (!context.getExternalContext().isResponseCommitted()) {
                    context.getExternalContext().redirect("index.xhtml");
                    context.responseComplete();
                }
                return;
            }

            String visualizarMapaId = FacesUtil.getRequestParameter("id");

            if (visualizarMapaId != null && !visualizarMapaId.isEmpty()) {
                this.mapa = this.mapaServico.pesquisarMapa(Long.parseLong(visualizarMapaId));
                if (this.mapa.getId() != null && this.mapa.getAnexo() != null) {
                    this.fileTemp = this.mapa.getAnexo().getNome();
                }

            } else {
                instanciar();
                // Carrega os mapas automaticamente ao inicializar a tela de pesquisa
                pesquisarMapas();
            }
        } catch (IOException ex) {
            System.err.println("Erro ao redirecionar: " + ex.getMessage());
        } catch (IllegalStateException ex) {
            System.err.println("Response já foi committed, não é possível redirecionar: " + ex.getMessage());
        }
    }

    public void instanciar() {
        this.mapa = new Mapas();
        this.mapas = new ArrayList<>();
        this.mapaDeletar = new Mapas();
        this.mapaPesquisar = new Mapas();
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
        System.err.println("file temp " + this.fileTemp);
        PrimeFaces.current().executeScript("atualizarImagem();");

    }

    public void removerImagem() {
        this.fileTemp = null;
        this.mapa.setAnexo(null);
        this.file = null;
        PrimeFaces.current().executeScript("atualizarImagem();");
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

   public void removerMapa() throws Exception {
        this.mapaServico.delete(this.mapa, Url.APAGAR_MAPA.getNome());
        Mensagem.successAndRedirect("pesquisarPlayer.xhtml");
        init();
    }

    public String getFileTemp() {
        return fileTemp;
    }

    public void setFileTemp(String fileTemp) {
        this.fileTemp = fileTemp;
    }

    public Mapas getMapaDeletar() {
        return mapaDeletar;
    }

    public void setMapaDeletar(Mapas mapaDeletar) {
        this.mapaDeletar = mapaDeletar;
    }

    public Mapas getMapaPesquisar() {
        return mapaPesquisar;
    }

    public void setMapaPesquisar(Mapas mapaPesquisar) {
        this.mapaPesquisar = mapaPesquisar;
    }

}
