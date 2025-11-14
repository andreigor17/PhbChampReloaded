package br.com.champ.Manager;

import br.com.champ.Enums.Url;
import br.com.champ.Modelo.Campeonato;
import br.com.champ.Modelo.Player;
import br.com.champ.Servico.TeamServico;
import java.io.Serializable;
import java.util.List;
import br.com.champ.Modelo.Team;
import br.com.champ.Servico.AnexoServico;
import br.com.champ.Servico.PlayerServico;
import br.com.champ.Utilitario.FacesUtil;
import br.com.champ.Utilitario.Mensagem;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.file.UploadedFile;

/**
 *
 * @author andre
 */
@ViewScoped
@Named
public class ManagerTeam extends ManagerBase {

    @EJB
    TeamServico teamServico;
    @EJB
    PlayerServico playerServico;
    @EJB
    AnexoServico anexoServico;

    private Team team;
    private List<Team> times;
    private List<Player> membros;
    private Player membro;
    private UploadedFile file;
    private StreamedContent imagem;
    private String fileTemp;
    private String termoBusca;
    private boolean buscando;
    private boolean carregandoJogadores;
    private boolean estadoInicial = true;
    private Team teamSelecionado;
    private List<Campeonato> trofeus;

    @PostConstruct
    public void init() {
        instanciar();

        String visualizarTeamId = FacesUtil
                .getRequestParameter("id");

        if (visualizarTeamId != null && !visualizarTeamId.isEmpty()) {
            this.team = this.teamServico.buscaTeam(Long.parseLong(visualizarTeamId));
            if (this.team.getId() != null && this.team.getAnexo() != null) {
                this.fileTemp = this.team.getAnexo().getNome();
            }
            if (this.team.getId() != null) {
                this.membros = this.team.getPlayers();
                // Carrega os troféus (campeonatos vencidos)
                carregarTrofeus();
            }
        }
    }

    public void instanciar() {
        this.team = new Team();
        this.teamSelecionado = new Team();
        this.times = new ArrayList<>();
        this.trofeus = new ArrayList<>();
        this.membros = new ArrayList<Player>();
        this.membro = new Player();
    }

    public Team getTeam() {
        return team;
    }

    public void setTeam(Team team) {
        this.team = team;
    }

    public List<Team> getTimes() {
        return times;
    }

    public void setTimes(List<Team> times) {
        this.times = times;
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

    public String getFileTemp() {
        return fileTemp;
    }

    public void setFileTemp(String fileTemp) {
        this.fileTemp = fileTemp;
    }

    public void adicionarMembro() {
        if (this.membros.contains(this.membro)) {
            Mensagem.error("Time ja possui esse jogador!");
            return;
        }
        this.membros.add(this.membro);
        this.membro = new Player();

    }

    public void doUpload(FileUploadEvent event) {
        this.team.setAnexo(anexoServico.fileUploadTemp(event));
        this.fileTemp = this.team.getAnexo().getNome();

    }

    public void salvarTeam() throws Exception {

        if (this.team.getAnexo() != null) {
            this.team.setAnexo(anexoServico.salvarAnexo(this.team.getAnexo()));
        }

        if (this.team.getId() == null) {
            this.team.setPlayers(this.membros);
            this.team = teamServico.save(this.team, null, Url.SALVAR_TIME.getNome());
            Mensagem.successAndRedirect("Time cadastrado com sucesso", "visualizarTime.xhtml?id=" + this.team.getId());
        } else {
            this.team.setPlayers(this.membros);
            this.team = teamServico.save(this.team, this.team.getId(), Url.ATUALIZAR_TIME.getNome());
            Mensagem.successAndRedirect("Time atualizado com sucesso", "visualizarTime.xhtml?id=" + this.team.getId());
        }

    }

    public void limparBusca() {
        termoBusca = "";
        this.times = new ArrayList<>();
        estadoInicial = true;
        buscando = false;
    }

    public void pesquisarTime() throws Exception {
        // Se o termo de busca for null, vazio ou menor que 2 caracteres, limpa a busca
        if (termoBusca == null || termoBusca.trim().length() < 2) {
            limparBusca();
            return;
        }

        estadoInicial = false;
        buscando = true;

        try {
            // Chama o serviço para pesquisar
            List<Team> resultado = teamServico.pesquisar(this.termoBusca.trim());
            
            // Garante que sempre temos uma lista (nunca null)
            if (resultado == null) {
                this.times = new ArrayList<>();
            } else {
                this.times = resultado;
            }
        } catch (Exception e) {
            System.err.println("Erro ao pesquisar team: " + e.getMessage());
            e.printStackTrace();
            // Em caso de erro, inicializa lista vazia
            this.times = new ArrayList<>();
        } finally {
            buscando = false;
        }
    }

    public void limpar() {
        instanciar();
    }

//    public void removeTime() {
//        this.teamServico.delete(this.team);
//        Mensagem.successAndRedirect("pesquisarTime.xhtml");
//        init();
//    }
    public List<Player> autoCompletarPlayer() {
        return playerServico.autoCompletePessoa();
    }

    public List<Player> getMembros() {
        return membros;
    }

    public void setMembros(List<Player> membros) {
        this.membros = membros;
    }

    public Player getMembro() {
        return membro;
    }

    public void setMembro(Player membro) {
        this.membro = membro;
    }

    public String getTermoBusca() {
        return termoBusca;
    }

    public void setTermoBusca(String termoBusca) {
        this.termoBusca = termoBusca;
    }

    public boolean isBuscando() {
        return buscando;
    }

    public void setBuscando(boolean buscando) {
        this.buscando = buscando;
    }

    public boolean isEstadoInicial() {
        return estadoInicial;
    }

    public void setEstadoInicial(boolean estadoInicial) {
        this.estadoInicial = estadoInicial;
    }

    public Team getTeamSelecionado() {
        return teamSelecionado;
    }

    public void setTeamSelecionado(Team teamSelecionado) {
        this.teamSelecionado = teamSelecionado;
    }

    public boolean isTemResultados() {
        return !estadoInicial && !buscando && this.times != null && !this.times.isEmpty();
    }

    public boolean isSemResultados() {
        return !estadoInicial && !buscando && (this.times == null || this.times.isEmpty());
    }

    public void deletar() {

    }

    public String getDataCriacaoFormatada() {
        if (this.team != null && this.team.getDataCriacao() != null && !this.team.getDataCriacao().isEmpty()) {
            try {
                OffsetDateTime odt = OffsetDateTime.parse(this.team.getDataCriacao());
                return odt.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
            } catch (Exception e) {
                System.err.println("Erro ao formatar data: " + e.getMessage());
                // Se não conseguir parsear, retorna os primeiros 10 caracteres (YYYY-MM-DD) e converte para dd-MM-yyyy
                try {
                    String dataStr = this.team.getDataCriacao();
                    if (dataStr.length() >= 10) {
                        String[] parts = dataStr.substring(0, 10).split("-");
                        if (parts.length == 3) {
                            return parts[2] + "-" + parts[1] + "-" + parts[0];
                        }
                    }
                } catch (Exception ex) {
                    System.err.println("Erro ao formatar data alternativa: " + ex.getMessage());
                }
                return this.team.getDataCriacao();
            }
        }
        return "";
    }

    /**
     * Verifica se o usuário logado é o capitão do time
     * @return true se o usuário logado é o capitão do time, false caso contrário
     */
    public boolean isCapitao() {
        if (this.team == null || this.team.getCapitao() == null) {
            return false;
        }
        Player player = getPlayerLogado();
        if (player == null || player.getId() == null) {
            return false;
        }
        return player.getId().equals(this.team.getCapitao().getId());
    }

    /**
     * Verifica se o usuário logado pode editar o time (é capitão OU admin)
     * @return true se pode editar, false caso contrário
     */
    public boolean podeEditarTime() {
        if (!isUsuarioLogado()) {
            return false;
        }
        return isCapitao() || isAdmin();
    }
    
    /**
     * Carrega os troféus (campeonatos vencidos) do time
     */
    public void carregarTrofeus() {
        try {
            if (this.team != null && this.team.getId() != null) {
                this.trofeus = teamServico.buscarTrofeus(this.team.getId());
                if (this.trofeus == null) {
                    this.trofeus = new ArrayList<>();
                }
                System.out.println("Troféus carregados: " + this.trofeus.size());
            } else {
                this.trofeus = new ArrayList<>();
            }
        } catch (Exception e) {
            System.err.println("Erro ao carregar troféus: " + e.getMessage());
            e.printStackTrace();
            this.trofeus = new ArrayList<>();
        }
    }
    
    public List<Campeonato> getTrofeus() {
        return trofeus;
    }
    
    public void setTrofeus(List<Campeonato> trofeus) {
        this.trofeus = trofeus;
    }

}
