package br.com.champ.Manager;

import br.com.champ.Enums.Url;
import br.com.champ.Modelo.Player;
import br.com.champ.Servico.TeamServico;
import java.io.Serializable;
import java.util.List;
import br.com.champ.Modelo.Team;
import br.com.champ.Servico.PlayerServico;
import br.com.champ.Utilitario.FacesUtil;
import br.com.champ.Utilitario.Mensagem;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import java.util.ArrayList;

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

    private Team team;
    private List<Team> times;
    private List<Player> membros;
    private Player membro;

    @PostConstruct
    public void init() {
        instanciar();

        String visualizarTeamId = FacesUtil
                .getRequestParameter("id");

        if (visualizarTeamId != null && !visualizarTeamId.isEmpty()) {
            this.team = this.teamServico.buscaTeam(Long.parseLong(visualizarTeamId));
            if (this.team.getId() != null) {
                this.membros = this.team.getPlayers();
            }
        }
    }

    public void instanciar() {
        this.team = new Team();
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

    public void adicionarMembro() {
        if (this.membros.contains(this.membro)) {
            Mensagem.error("Time ja possui esse jogador!");
            return;
        }
        this.membros.add(this.membro);
        this.membro = new Player();

    }

    public void salvarTeam() throws Exception {

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

    public void pesquisarTime() throws Exception {
        this.times = teamServico.pesquisar(this.team.getNome());
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

}
