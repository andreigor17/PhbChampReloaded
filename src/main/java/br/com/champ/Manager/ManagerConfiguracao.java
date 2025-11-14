/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.champ.Manager;

import br.com.champ.Modelo.Configuracao;
import jakarta.faces.context.FacesContext;
import br.com.champ.Servico.ConfiguracaoServico;
import br.com.champ.Utilitario.APIPath;
import br.com.champ.Utilitario.Mensagem;
import br.com.champ.vo.CSGOServerVo;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 *
 * @author andre
 */
@ViewScoped
@Named
public class ManagerConfiguracao extends ManagerBase {

    private String menu = "GERAL";
    private Configuracao configuracao;
    @EJB
    private ConfiguracaoServico configuracaoServico;
    private String path;
    private boolean csgoServerStatus;
    private CSGOServerVo csVo;
    private boolean start = false;

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
            
            instanciar();
            this.configuracao = this.configuracaoServico.buscaConfig();
            this.path = APIPath.pathToAPI();
            //this.csVo = this.configuracaoServico.csgoServerStatus();
            //System.out.println("cs " + this.csVo.getHealth());
            //this.csgoServerStatus = this.configuracaoServico.csgoServerStatus();

        } catch (IOException ex) {
            System.err.println("Erro ao redirecionar: " + ex.getMessage());
        } catch (IllegalStateException ex) {
            System.err.println("Response já foi committed, não é possível redirecionar: " + ex.getMessage());
        } catch (Exception ex) {
            Logger.getLogger(ManagerPlayer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void instanciar() throws Exception {
        this.configuracao = new Configuracao();
        this.csVo = new CSGOServerVo();
    }

    public void start() {
        this.start = true;
    }

    public void renderizarMenu(String menu) {
        this.menu = menu;
    }

    public String getMenu() {
        return menu;
    }

    public void setMenu(String menu) {
        this.menu = menu;
    }

    public Configuracao getConfiguracao() {
        return configuracao;
    }

    public void setConfiguracao(Configuracao configuracao) {
        this.configuracao = configuracao;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void teste() throws Exception {
        if (start) {
            this.csVo = this.configuracaoServico.csgoServerStatus();
        }
    }

    public void updatePath() throws IOException {
        APIPath.updateFile(this.path);
        Mensagem.successAndRedirect("Path atualizado com sucesso!", "configuracaoAPI.xhtml");
    }

    public void salvar() {
        try {
            Configuracao c = new Configuracao();

            if (this.configuracao.getCaminhoApi() == null) {
                Mensagem.error("O caminho da API não pode estar vazio!");
                return;
            }
            c = configuracaoServico.save(this.configuracao, this.configuracao.getId());

            Mensagem.successAndRedirect("Configuração salva com  sucesso", "configuracao.xhtml");

        } catch (Exception ex) {
            Logger.getLogger(ManagerConfiguracao.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public boolean isCsgoServerStatus() {
        return csgoServerStatus;
    }

    public void setCsgoServerStatus(boolean csgoServerStatus) {
        this.csgoServerStatus = csgoServerStatus;
    }

    public CSGOServerVo getCsVo() {
        return csVo;
    }

    public void setCsVo(CSGOServerVo csVo) {
        this.csVo = csVo;
    }

    public boolean isStart() {
        return start;
    }

    public void setStart(boolean start) {
        this.start = start;
    }

}
