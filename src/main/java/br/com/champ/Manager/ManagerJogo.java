/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package br.com.champ.Manager;

import br.com.champ.Enums.Url;
import br.com.champ.Modelo.Jogo;
import br.com.champ.Modelo.VersaoJogo;
import br.com.champ.Servico.AnexoServico;
import br.com.champ.Servico.JogoServico;
import br.com.champ.Servico.VersaoJogoServico;
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

/**
 *
 * @author andre
 */
@ViewScoped
@Named
public class ManagerJogo extends ManagerBase {

    @EJB
    private JogoServico jogoServico;
    @EJB
    private VersaoJogoServico versaoServico;
    @EJB
    private AnexoServico anexoServico;
    private Jogo jogo;
    private List<Jogo> jogos;
    private List<VersaoJogo> versoes;
    private String fileTemp;

    @PostConstruct
    public void init() {
        try {
            // Verifica se usuário é admin
            if (!isAdmin()) {
                FacesContext.getCurrentInstance().getExternalContext().redirect("login.xhtml");
                return;
            }

            String visualizarJogoId = FacesUtil.getRequestParameter("id");

            if (visualizarJogoId != null && !visualizarJogoId.isEmpty()) {
                this.jogo = this.jogoServico.pesquisarJogo(Long.parseLong(visualizarJogoId));
                if (this.jogo.getId() != null) {
                    this.versoes = this.jogo.getVersoes();
                    if (this.jogo.getAnexo() != null) {
                        this.fileTemp = this.jogo.getAnexo().getNome();
                    }
                }

            } else {
                instanciar();
            }
        } catch (IOException ex) {
            System.err.println("Erro ao redirecionar: " + ex.getMessage());
        }
    }

    public void instanciar() {
        this.jogo = new Jogo();
        this.jogos = new ArrayList<>();
        this.versoes = new ArrayList<>();
        this.fileTemp = null;
    }

    public Jogo getJogo() {
        return jogo;
    }

    public void setJogo(Jogo jogo) {
        this.jogo = jogo;
    }

    public List<Jogo> getJogos() {
        return jogos;
    }

    public void setJogos(List<Jogo> jogos) {
        this.jogos = jogos;
    }

    public List<VersaoJogo> getVersoes() {
        return versoes;
    }

    public void setVersoes(List<VersaoJogo> versoes) {
        this.versoes = versoes;
    }

    public String getFileTemp() {
        return fileTemp;
    }

    public void setFileTemp(String fileTemp) {
        this.fileTemp = fileTemp;
    }

    public void doUpload(FileUploadEvent event) {
        this.jogo.setAnexo(anexoServico.fileUploadTemp(event));
        if (this.jogo.getAnexo() != null) {
            this.fileTemp = this.jogo.getAnexo().getNome();
        }
        PrimeFaces.current().executeScript("atualizarLogo();");
    }

    public void removerLogo() {
        if (this.jogo != null) {
            this.jogo.setAnexo(null);
        }
        this.fileTemp = null;
        PrimeFaces.current().ajax().update("formJogo:logoPanel", "formJogo:uploadPanel");
    }

    public void limpar() {
        instanciar();
    }

    public void pesquisarJogos() {
        try {
            this.jogos = jogoServico.pesquisar();
        } catch (Exception ex) {
            System.err.println(ex);
        }
    }

    public void salvar() {
        try {
            if (this.jogo.getAnexo() != null) {
                this.jogo.setAnexo(anexoServico.salvarAnexo(this.jogo.getAnexo()));
            }
        } catch (Exception ex) {
            System.err.println("Erro ao salvar anexo do jogo: " + ex);
        }

        if (this.jogo.getId() != null) {
            try {
                this.jogo = jogoServico.save(this.jogo, this.jogo.getId(), Url.ATUALIZAR_JOGO.getNome());
            } catch (Exception ex) {
                System.err.println(ex);
            }
            Mensagem.successAndRedirect("Jogo atualizado com sucesso!", "visualizarJogo.xhtml?id=" + this.jogo.getId());

        } else {
            try {
                this.jogo = jogoServico.save(this.jogo, null, Url.SALVAR_JOGO.getNome());
            } catch (Exception ex) {
                System.err.println(ex);
            }
            Mensagem.successAndRedirect("Jogo criado com sucesso!", "visualizarJogo.xhtml?id=" + this.jogo.getId());

        }
        if (this.jogo != null && this.jogo.getAnexo() != null) {
            this.fileTemp = this.jogo.getAnexo().getNome();
        }
    }

    public void excluir() {
        try {
            this.jogo.setActive(Boolean.FALSE);
            jogoServico.save(this.jogo, null, Url.ATUALIZAR_JOGO.getNome());
        } catch (Exception ex) {
            System.err.println(ex);
        }
        Mensagem.successAndRedirect("Jogo excluído com sucesso!", "pesquisarJogos.xhtml");
    }

}
