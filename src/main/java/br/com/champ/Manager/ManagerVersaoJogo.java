/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.champ.Manager;

import br.com.champ.Enums.Url;
import br.com.champ.Modelo.VersaoJogo;
import br.com.champ.Servico.AnexoServico;
import br.com.champ.Servico.VersaoJogoServico;
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

/**
 *
 * @author andre
 */
@ViewScoped
@Named
public class ManagerVersaoJogo implements Serializable {

    @EJB
    private VersaoJogoServico versaoJogoServico;
    @EJB
    AnexoServico anexoServico;
    private VersaoJogo versao;
    private String fileTemp;
    private List<VersaoJogo> versoes;

    @PostConstruct
    public void init() {
        String visualizarVersaoId = FacesUtil
                .getRequestParameter("id");

        if (visualizarVersaoId != null && !visualizarVersaoId.isEmpty()) {
            this.versao = this.versaoJogoServico.pesquisarJogo(Long.parseLong(visualizarVersaoId));
            if (this.versao.getId() != null && this.versao.getAnexo() != null) {
                this.fileTemp = this.versao.getAnexo().getNome();
            }

        } else {
            instanciar();
        }

    }

    public void instanciar() {
        this.versao = new VersaoJogo();
        this.versoes = new ArrayList<>();
    }

    public VersaoJogo getVersao() {
        return versao;
    }

    public void setVersao(VersaoJogo versao) {
        this.versao = versao;
    }

    public List<VersaoJogo> getVersoes() {
        return versoes;
    }

    public void setVersoes(List<VersaoJogo> versoes) {
        this.versoes = versoes;
    }

    public void limpar() {
        instanciar();
    }

    public void pesquisarVersoes() {
        try {
            this.versoes = versaoJogoServico.pesquisar();
        } catch (Exception ex) {
            System.err.println(ex);
        }
    }

    public void doUpload(FileUploadEvent event) {
        this.versao.setAnexo(anexoServico.fileUpload(event));

    }

    public void salvar() {

        if (this.versao.getId() != null) {
            try {
                this.versao = versaoJogoServico.save(this.versao, this.versao.getId(), Url.ATUALIZAR_VERSAO.getNome());
                this.versao = versaoJogoServico.pesquisarJogo(this.versao.getId());
            } catch (Exception ex) {
                System.err.println(ex);
            }
            Mensagem.successAndRedirect("Versão atualizada com sucesso!", "visualizarVersaoJogo.xhtml?id=" + this.versao.getId());

        } else {
            try {
                this.versao = versaoJogoServico.save(this.versao, null, Url.SALVAR_VERSAO.getNome());
            } catch (Exception ex) {
                System.err.println(ex);
            }
            Mensagem.successAndRedirect("Versão criada com sucesso!", "visualizarVersaoJogo.xhtml?id=" + this.versao.getId());

        }
    }

    public void excluir() {
        try {
            this.versao.setActive(Boolean.FALSE);
            versaoJogoServico.save(this.versao, null, Url.ATUALIZAR_VERSAO.getNome());
        } catch (Exception ex) {
            System.err.println(ex);
        }
        Mensagem.successAndRedirect("Versão excluída com sucesso!", "pesquisarVersoes.xhtml");
    }

    public String getFileTemp() {
        return fileTemp;
    }

    public void setFileTemp(String fileTemp) {
        this.fileTemp = fileTemp;
    }

}
