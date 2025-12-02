package br.com.champ.Manager;

import br.com.champ.Enums.Url;
import br.com.champ.Modelo.Anexo;
import br.com.champ.Servico.AnexoServico;
import br.com.champ.Utilitario.Mensagem;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.file.UploadedFile;

/**
 * Manager para gerenciamento de Demos do CS2
 * @author andre
 */
@ViewScoped
@Named
public class ManagerDemo extends ManagerBase {

    @EJB
    private AnexoServico anexoServico;
    
    private List<Anexo> demos;
    private Anexo demoSelecionado;
    private UploadedFile uploadedFile;
    private boolean uploading = false;

    @PostConstruct
    public void init() {
        try {
            instanciar();
            carregarDemos();
        } catch (Exception e) {
            Logger.getLogger(ManagerDemo.class.getName()).log(Level.SEVERE, "Erro ao inicializar ManagerDemo", e);
            Mensagem.error("Erro ao inicializar página de demos");
        }
    }

    /**
     * Inicializa variáveis
     */
    public void instanciar() {
        this.demos = new ArrayList<>();
        this.demoSelecionado = null;
        this.uploading = false;
    }

    /**
     * Carrega lista de demos do banco de dados
     */
    public void carregarDemos() {
        try {
            // Busca todos os anexos que são demos (você pode adicionar um filtro específico)
            List<Anexo> todosAnexos = anexoServico.findAll(null, Url.BUSCAR_TODOS_ANEXO.getNome());
            
            // Filtra apenas arquivos .dem e .zip
            if (todosAnexos != null) {
                List<Anexo> demosFiltered = new ArrayList<>();
                for (Anexo anexo : todosAnexos) {
                    if (anexo.getNome() != null && 
                        (anexo.getNome().toLowerCase().endsWith(".dem") || 
                         anexo.getNome().toLowerCase().endsWith(".zip"))) {
                        demosFiltered.add(anexo);
                    }
                }
                this.demos = demosFiltered;
            } else {
                this.demos = new ArrayList<>();
            }
        } catch (Exception e) {
            Logger.getLogger(ManagerDemo.class.getName()).log(Level.SEVERE, "Erro ao carregar demos", e);
            Mensagem.error("Erro ao carregar demos");
            this.demos = new ArrayList<>();
        }
    }

    /**
     * Faz upload de um novo demo
     */
    public void uploadDemo(FileUploadEvent event) {
        try {
            uploading = true;
            
            UploadedFile file = event.getFile();
            String fileName = file.getFileName();
            long fileSize = file.getSize();
            
            // Valida extensão do arquivo
            if (!fileName.toLowerCase().endsWith(".dem") && 
                !fileName.toLowerCase().endsWith(".zip")) {
                Mensagem.error("Apenas arquivos .dem ou .zip são permitidos");
                uploading = false;
                return;
            }
            
            // Valida tamanho do arquivo (máximo 50MB)
            long maxSize = 500 * 1024 * 1024; // 50 MB
            System.err.println("max size " + maxSize);
            System.err.println("size " + fileSize);
            if (fileSize > maxSize) {
                Mensagem.error("Arquivo muito grande. Tamanho máximo permitido: 50 MB. " +
                        "Arquivo atual: " + String.format("%.2f MB", fileSize / (1024.0 * 1024.0)));
                uploading = false;
                return;
            }
            
            // Avisa sobre arquivos grandes
            if (fileSize > 10 * 1024 * 1024) { // Maior que 10MB
                System.out.println("Processando arquivo grande: " + fileName + 
                        " (" + String.format("%.2f MB", fileSize / (1024.0 * 1024.0)) + ")");
            }
            
            // Salva o anexo
            Anexo demo = anexoServico.fileUpload(event);
            
            if (demo != null && demo.getId() != null) {
                Mensagem.success("Demo enviado com sucesso!");
                carregarDemos();
            } else {
                Mensagem.error("Erro ao salvar demo");
            }
            
        } catch (Exception e) {
            Logger.getLogger(ManagerDemo.class.getName()).log(Level.SEVERE, "Erro no upload do demo", e);
            Mensagem.error("Erro ao fazer upload do demo: " + e.getMessage());
        } finally {
            uploading = false;
        }
    }

    /**
     * Visualiza um demo no visualizador
     */
    public String visualizarDemo(Anexo demo) {
        try {
            if (demo == null || demo.getId() == null) {
                Mensagem.error("Demo inválido");
                return null;
            }
            
            // Redireciona para o visualizador com o ID do demo
            return "demo-viewer.xhtml?faces-redirect=true&amp;anexoId=" + demo.getId();
            
        } catch (Exception e) {
            Logger.getLogger(ManagerDemo.class.getName()).log(Level.SEVERE, "Erro ao visualizar demo", e);
            Mensagem.error("Erro ao abrir visualizador");
            return null;
        }
    }

    /**
     * Baixa um demo
     */
    public void downloadDemo(Anexo demo) {
        try {
            if (demo == null || demo.getId() == null) {
                Mensagem.error("Demo inválido");
                return;
            }
            
            // Busca o anexo completo com a imagem
            Anexo anexoCompleto = anexoServico.find(demo, demo.getId(), Url.BUSCAR_ANEXO.getNome());
            
            if (anexoCompleto != null && anexoCompleto.getImagem() != null) {
                // Prepara o download
                FacesContext fc = FacesContext.getCurrentInstance();
                jakarta.servlet.http.HttpServletResponse response = 
                    (jakarta.servlet.http.HttpServletResponse) fc.getExternalContext().getResponse();
                
                response.reset();
                response.setContentType("application/octet-stream");
                response.setContentLength(anexoCompleto.getImagem().length);
                response.setHeader("Content-Disposition", 
                    "attachment; filename=\"" + anexoCompleto.getNomeExibicao() + "\"");
                
                response.getOutputStream().write(anexoCompleto.getImagem());
                response.getOutputStream().flush();
                response.getOutputStream().close();
                
                fc.responseComplete();
            } else {
                Mensagem.error("Demo não encontrado ou sem conteúdo");
            }
            
        } catch (Exception e) {
            Logger.getLogger(ManagerDemo.class.getName()).log(Level.SEVERE, "Erro ao fazer download do demo", e);
            Mensagem.error("Erro ao fazer download");
        }
    }

    /**
     * Exclui um demo
     */
    public void excluirDemo(Anexo demo) {
        try {
            if (demo == null || demo.getId() == null) {
                Mensagem.error("Demo inválido");
                return;
            }
            
            // Exclui o anexo
            anexoServico.delete(demo, demo.getId(), Url.EXCLUIR_ANEXO.getNome());
            
            Mensagem.success("Demo excluído com sucesso!");
            carregarDemos();
            
        } catch (Exception e) {
            Logger.getLogger(ManagerDemo.class.getName()).log(Level.SEVERE, "Erro ao excluir demo", e);
            Mensagem.error("Erro ao excluir demo");
        }
    }

    /**
     * Formata o tamanho do arquivo
     */
    public String formatarTamanho(byte[] imagem) {
        if (imagem == null) return "0 KB";
        
        double bytes = imagem.length;
        if (bytes < 1024) return String.format("%.0f B", bytes);
        if (bytes < 1024 * 1024) return String.format("%.2f KB", bytes / 1024);
        return String.format("%.2f MB", bytes / (1024 * 1024));
    }

    /**
     * Retorna ícone baseado no tipo de arquivo
     */
    public String getIconeArquivo(String nomeArquivo) {
        if (nomeArquivo == null) return "pi pi-file";
        if (nomeArquivo.toLowerCase().endsWith(".dem")) return "pi pi-video";
        if (nomeArquivo.toLowerCase().endsWith(".zip")) return "pi pi-folder";
        return "pi pi-file";
    }

    // Getters e Setters
    public List<Anexo> getDemos() {
        return demos;
    }

    public void setDemos(List<Anexo> demos) {
        this.demos = demos;
    }

    public Anexo getDemoSelecionado() {
        return demoSelecionado;
    }

    public void setDemoSelecionado(Anexo demoSelecionado) {
        this.demoSelecionado = demoSelecionado;
    }

    public UploadedFile getUploadedFile() {
        return uploadedFile;
    }

    public void setUploadedFile(UploadedFile uploadedFile) {
        this.uploadedFile = uploadedFile;
    }

    public boolean isUploading() {
        return uploading;
    }

    public void setUploading(boolean uploading) {
        this.uploading = uploading;
    }
}

