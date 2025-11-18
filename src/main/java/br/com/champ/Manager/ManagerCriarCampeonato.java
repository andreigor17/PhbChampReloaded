package br.com.champ.Manager;

import br.com.champ.Enums.Url;
import br.com.champ.Modelo.Campeonato;
import br.com.champ.Modelo.Estatisticas;
import br.com.champ.Modelo.ItemPartida;
import br.com.champ.Modelo.Jogo;
import br.com.champ.Modelo.Partida;
import br.com.champ.Modelo.Player;
import br.com.champ.Modelo.Team;
import br.com.champ.Servico.CampeonatoServico;
import br.com.champ.Servico.EstatisticaServico;
import br.com.champ.Servico.FaseServico;
import br.com.champ.Servico.JogoServico;
import br.com.champ.Servico.PartidaServico;
import br.com.champ.Servico.PlayerServico;
import br.com.champ.Servico.TeamServico;
import br.com.champ.Utilitario.FacesUtil;
import br.com.champ.Utilitario.Mensagem;
import br.com.champ.Utilitario.PartidaUtils;
import br.com.champ.Utilitario.Utils;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.primefaces.event.FlowEvent;

/**
 *
 * @author andre
 */
@ViewScoped
@Named
public class ManagerCriarCampeonato extends ManagerBase {

    @EJB
    TeamServico teamServico;
    @EJB
    PlayerServico playerServico;
    @EJB
    CampeonatoServico campeonatoServico;
    @EJB
    EstatisticaServico estatisticaServico;
    @EJB
    PartidaServico partidaServico;
    @EJB
    EstatisticaServico estatisticasServico;
    @EJB
    JogoServico jogoServico;
    @EJB
    FaseServico faseServico;
    private Campeonato camp;
    private List<Campeonato> camps;
    private List<Team> times;
    private Team time;
    private List<Estatisticas> estatisticasTime;
    private Estatisticas estatistica;
    private Date dataCamp;
    private Date dataFinal;
    private int qtdItensPartidas;
    private List<ItemPartida> itemPartidas;
    private Partida partida;
    private List<Estatisticas> estsGerais;
    private boolean skip;
    private Player membro;
    private List<Player> membros;
    private Jogo jogo;
    private boolean faseInscricao;

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

        String visualizarCampId = FacesUtil
                .getRequestParameter("id");

        if (visualizarCampId != null && !visualizarCampId.isEmpty()) {
            this.camp = this.campeonatoServico.buscaCamp(Long.parseLong(visualizarCampId));
            }
        } catch (IOException ex) {
            System.err.println("Erro ao redirecionar: " + ex.getMessage());
        } catch (IllegalStateException ex) {
            System.err.println("Response já foi committed, não é possível redirecionar: " + ex.getMessage());
        }

    }

    public void instanciar() {
        this.camp = new Campeonato();
        this.camps = new ArrayList<>();
        this.time = new Team();
        this.times = new ArrayList<>();
        this.estatisticasTime = new ArrayList<>();
        this.dataCamp = new Date();
        this.itemPartidas = new ArrayList<>();
        this.partida = new Partida();
        this.estsGerais = new ArrayList<>();
        this.membro = new Player();
        this.membros = new ArrayList<>();
        this.jogo = new Jogo();
        this.faseInscricao = true;

    }

    public boolean isFaseInscricao() {
        return faseInscricao;
    }

    public void setFaseInscricao(boolean faseInscricao) {
        this.faseInscricao = faseInscricao;
    }

    public Jogo getJogo() {
        return jogo;
    }

    public void setJogo(Jogo jogo) {
        this.jogo = jogo;
    }

    public Campeonato getCamp() {
        return camp;
    }

    public void setCamp(Campeonato camp) {
        this.camp = camp;
    }

    public List<Campeonato> getCamps() {
        return camps;
    }

    public void setCamps(List<Campeonato> camps) {
        this.camps = camps;
    }

    public List<Team> getTimes() {
        return times;
    }

    public void setTimes(List<Team> times) {
        this.times = times;
    }

    public Team getTime() {
        return time;
    }

    public void setTime(Team time) {
        this.time = time;
    }

    public List<Estatisticas> getEstatisticasTime() {
        return estatisticasTime;
    }

    public void setEstatisticasTime(List<Estatisticas> estatisticasTime) {
        this.estatisticasTime = estatisticasTime;
    }

    public Estatisticas getEstatistica() {
        return estatistica;
    }

    public void setEstatistica(Estatisticas estatistica) {
        this.estatistica = estatistica;
    }

    public Date getDataCamp() {
        return dataCamp;
    }

    public void setDataCamp(Date dataCamp) {
        this.dataCamp = dataCamp;
    }

    public int getQtdItensPartidas() {
        return qtdItensPartidas;
    }

    public void setQtdItensPartidas(int qtdItensPartidas) {
        this.qtdItensPartidas = qtdItensPartidas;
    }

    public List<ItemPartida> getItemPartidas() {
        return itemPartidas;
    }

    public void setItemPartidas(List<ItemPartida> itemPartidas) {
        this.itemPartidas = itemPartidas;
    }

    public Partida getPartida() {
        return partida;
    }

    public void setPartida(Partida partida) {
        this.partida = partida;
    }

    public List<Estatisticas> getEstsGerais() {
        return estsGerais;
    }

    public void setEstsGerais(List<Estatisticas> estsGerais) {
        this.estsGerais = estsGerais;
    }

    public void adicionarMembro() {
        this.membros.add(this.membro);
        this.membro = new Player();

    }

    public void adicionarTime() {
        this.times.add(this.time);
        this.time = new Team();

    }

    public List<Player> autoCompletarPlayer() {
        return playerServico.autoCompletePessoa();
    }

    public void salvar() {
        try {
            this.camp = campeonatoServico.save(this.camp, null, Url.SALVAR_CAMPEONATO.getNome());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        Mensagem.successAndRedirect("Camp salvo", "visualizarCampeonato.xhtml?id=" + this.camp.getId());
    }

//    public void salvarCampeonato() throws Exception {
//
//        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
//        Fase fase = new Fase();
//        List<Fase> fases = new ArrayList<>();
//
//        String dataFormatada = formatter.format(this.dataCamp);
//        String dataFormatadaFinal = formatter.format(this.dataFinal);
//
//        this.camp.setDataCamp(dataFormatada);
//        this.camp.setDataFinal(dataFormatadaFinal);
//        this.camp.setJogo(this.jogo);
//
//        this.camp.setTeams(this.times);
//        this.camp.setPlayers(this.membros);
//        if (faseInscricao) {
//
//            this.camp.setStatus(StatusCamp.INSCRICOES_ABERTAS);
//            this.camp = campeonatoServico.save(this.camp, null, Url.SALVAR_CAMPEONATO.getNome());
//            Mensagem.successAndRedirect("Camp salvo", "preCampeonato.xhtml?preCampId=" + this.camp.getId());
//        } else {
//
//            this.camp.setStatus(StatusCamp.EM_ANDAMENTO);
//            this.camp = campeonatoServico.save(this.camp, null, Url.SALVAR_CAMPEONATO.getNome());
//            this.camp = campeonatoServico.buscaCamp(this.camp.getId());
//            if (this.camp != null && !this.camp.getTipoCampeonato().equals(TipoCampeonato.MATA_MATA)) {
//                this.camp.setPartidas(gerarPartidas(this.camp.getId(), this.camp));
//            } else {
//                this.camp.setPartidas(gerarPartidasMataMata(this.camp.getId(), this.camp));
//                fase.setNome(PartidaUtils.obterFaseAtual(this.camp.getTeams().size()));
//                fase.setPartidas(this.camp.getPartidas());
//                fase.setIdCamp(this.camp.getId());
//                fase = faseServico.save(fase, null, Url.SALVAR_FASE.getNome());
//                fases.add(fase);
//                this.camp.setFasesCamp(fases);
//
//            }
//            this.camp = campeonatoServico.save(this.camp, this.camp.getId(), Url.ATUALIZAR_CAMPEONATO.getNome());
//            Mensagem.successAndRedirect("Camp salvo", "visualizarCampeonato.xhtml?id=" + this.camp.getId());
//        }
//    }

    public void salvarPreCamp() {

    }

    public List<Team> autoCompletarTime() throws Exception {
        return teamServico.autoCompleteTime();
    }

    public void adicionarCamp() {
        this.times.add(this.time);
        this.time = new Team();

    }

    public List<Partida> gerarPartidas(Long id, Campeonato camp) throws Exception {
        List<Partida> matches = new ArrayList<>();
        Partida match = new Partida();
        if (Utils.isNotEmpty(this.times)) {
            for (int i = 0; i < this.times.size() - 1; i++) {
                for (int j = i + 1; j < this.times.size(); j++) {
                    match = salvarPartidaClassica(this.times.get(i), this.times.get(j), id, camp, null, null, 0);
                    matches.add(match);
                    this.partida = new Partida();
                }
            }
        }
        if (Utils.isNotEmpty(this.membros)) {
            for (int i = 0; i < this.membros.size() - 1; i++) {
                for (int j = i + 1; j < this.membros.size(); j++) {
                    match = salvarPartidaClassica(null, null, id, camp, this.membros.get(i), this.membros.get(j), 0);
                    matches.add(match);
                    this.partida = new Partida();
                }
            }
        }
        Collections.shuffle(matches);
        return matches;
    }

    public static int calcularPartidas(int n) {
        return (n / 2) + 2 + 1 + 1;
    }

    public List<Partida> gerarPartidasMataMata(Long id, Campeonato camp) throws Exception {
        List<Partida> matches = new ArrayList<>();
        List<Integer> contadores = new ArrayList<>();
        Partida match = new Partida();
        if (Utils.isNotEmpty(this.times)) {
            Collections.shuffle(this.times);
            int contador = 0;
            int contadorProxFase = 0;
            for (int i = 0; i < times.size(); i += 2) {
                contador++;
                match = salvarPartidaClassica(this.times.get(i), this.times.get(i + 1), id, camp, null, null, contador);
                matches.add(match);
                this.partida = new Partida();
                match = new Partida();
                contadores.add(contador);

            }

        }
        Collections.shuffle(matches);
        return matches;
    }

    public Partida salvarPartidaClassica(Team t1, Team t2, Long id, Campeonato camp, Player p1, Player p2, int contador) {
        try {

            List<Estatisticas> estsTeam1 = new ArrayList<Estatisticas>();
            List<Estatisticas> estsTeam2 = new ArrayList<Estatisticas>();

            Team team1 = t1;
            Team team2 = t2;

            if (Utils.isNotEmpty(t1) && Utils.isNotEmpty(t2)) {
                this.itemPartidas = PartidaUtils.gerarPartidasTimes(this.partida, id, team1, team2, this.qtdItensPartidas);
            }

            if (Utils.isNotEmpty(p1) && Utils.isNotEmpty(p2)) {
                this.itemPartidas = PartidaUtils.gerarPartidasPlayers(this.partida, id, p1, p2, this.qtdItensPartidas);
            }

            this.partida.setItemPartida(this.itemPartidas);
            this.partida.setJogo(this.camp.getJogo());
            this.partida.setContador(contador);
            this.partida = partidaServico.salvar(this.partida, null, Url.SALVAR_PARTIDA.getNome());

            this.partida = partidaServico.pesquisar(this.partida.getId());
            System.err.println("Partida ID " + this.partida.getId());
            List<ItemPartida> it = this.partida.getItemPartida();

            for (ItemPartida ip : this.partida.getItemPartida()) {
                System.err.println("passando no for de item, setando a partida no item");
                ip.setPartida(this.partida.getId());

            }

            this.partida = partidaServico.salvar(this.partida, this.partida.getId(), Url.ATUALIZAR_PARTIDA.getNome());
            System.err.println("atualizou a partida");

            if (Utils.isNotEmpty(t1) && Utils.isNotEmpty(t2)) {
                for (ItemPartida i : it) {
                    for (Player playerTime1 : team1.getPlayers()) {
                        Estatisticas estatisticas = new Estatisticas();
                        estatisticas.setPlayer(playerTime1);
                        estatisticas.setTeam(team1);
                        estatisticas.setItemPartida(i);
                        estatisticas.setCampeonato(camp);
                        estsTeam1.add(estatisticas);

                    }
                    this.estsGerais.addAll(estsTeam1);

                    for (Player playerTime2 : team2.getPlayers()) {
                        Estatisticas estatisticas = new Estatisticas();
                        estatisticas.setPlayer(playerTime2);
                        estatisticas.setTeam(team2);
                        estatisticas.setItemPartida(i);
                        estatisticas.setCampeonato(camp);
                        estsTeam2.add(estatisticas);
                    }
                    this.estsGerais.addAll(estsTeam2);

                    for (Estatisticas e : this.estsGerais) {
                        estatisticasServico.salvar(e, null, Url.SALVAR_ESTATISTICA.getNome());
                    }

                    estsTeam1 = new ArrayList<Estatisticas>();
                    estsTeam2 = new ArrayList<Estatisticas>();
                    this.estsGerais = new ArrayList<Estatisticas>();

                }
            }

            if (Utils.isNotEmpty(p1) && Utils.isNotEmpty(p2)) {
                for (ItemPartida i : it) {

                    Estatisticas estatisticas = new Estatisticas();
                    estatisticas.setPlayer(p1);
                    estatisticas.setItemPartida(i);
                    estatisticas.setCampeonato(camp);
                    estsTeam1.add(estatisticas);

                    this.estsGerais.addAll(estsTeam1);

                    Estatisticas estatisticas2 = new Estatisticas();
                    estatisticas2.setPlayer(p2);
                    estatisticas2.setTeam(team2);
                    estatisticas2.setItemPartida(i);
                    estatisticas2.setCampeonato(camp);
                    estsTeam2.add(estatisticas2);

                    this.estsGerais.addAll(estsTeam2);

                    for (Estatisticas e : this.estsGerais) {
                        estatisticasServico.salvar(e, null, Url.SALVAR_ESTATISTICA.getNome());
                    }

                    estsTeam1 = new ArrayList<Estatisticas>();
                    estsTeam2 = new ArrayList<Estatisticas>();
                    this.estsGerais = new ArrayList<Estatisticas>();

                }
            }

        } catch (Exception ex) {
            System.err.println(ex);
        }

        return this.partida;
    }

    public void limpar() {
        instanciar();
    }

//    public void removeCamp() {
//        this.campeonatoServico.delete(this.camp);
//        Mensagem.successAndRedirect("pesquisarCampeonato.xhtml");
//        init();
//    }
    public void pesquisarCamp() throws Exception {
        this.camps = campeonatoServico.pesquisar();
    }

    public boolean isSkip() {
        return skip;
    }

    public void setSkip(boolean skip) {
        this.skip = skip;
    }

    public String onFlowProcess(FlowEvent event) {
        if (skip) {
            skip = false; //reset in case user goes back
            return "confirm";
        } else {
            return event.getNewStep();
        }
    }

    public Date getDataFinal() {
        return dataFinal;
    }

    public void setDataFinal(Date dataFinal) {
        this.dataFinal = dataFinal;
    }

    public Player getMembro() {
        return membro;
    }

    public void setMembro(Player membro) {
        this.membro = membro;
    }

    public List<Player> getMembros() {
        return membros;
    }

    public void setMembros(List<Player> membros) {
        this.membros = membros;
    }

    public List<Jogo> jogos() {
        try {
            return jogoServico.pesquisar();
        } catch (Exception ex) {
            Logger.getLogger(ManagerCriarCampeonato.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

}
