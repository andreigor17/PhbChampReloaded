package br.com.champ.Manager;

import br.com.champ.Enums.Url;
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
public class ManagerTeam implements Serializable {

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
            }
        }
    }

    public void instanciar() {
        this.team = new Team();
        this.teamSelecionado = new Team();
        this.times = new ArrayList<>();
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
        if (termoBusca == null || termoBusca.trim().length() < 2) {
            limparBusca();
            this.termoBusca = "";
            return;
        }

        estadoInicial = false;
        buscando = true;

        try {

            this.times = teamServico.pesquisar(this.termoBusca);
        } catch (Exception e) {
            this.times = new ArrayList<>();
            e.printStackTrace();
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

}
