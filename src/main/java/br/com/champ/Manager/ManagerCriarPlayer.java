/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.champ.Manager;

import br.com.champ.Enums.Funcoes;
import br.com.champ.Enums.Url;
import br.com.champ.Modelo.Anexo;
import br.com.champ.Modelo.Configuracao;
import br.com.champ.Modelo.Jogo;
import br.com.champ.Modelo.Player;
import br.com.champ.Servico.AnexoServico;
import br.com.champ.Servico.ConfiguracaoServico;
import br.com.champ.Servico.JogoServico;
import br.com.champ.Servico.LoginServico;
import br.com.champ.Servico.PlayerServico;
import br.com.champ.Utilitario.FacesUtil;
import br.com.champ.Utilitario.Mensagem;
import br.com.champ.vo.LoginVo;
import br.com.champ.vo.PlayerSteamVo;
import br.com.champ.vo.UsuarioVo;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
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
public class ManagerCriarPlayer implements Serializable {

    @EJB
    PlayerServico playerServico;
    @EJB
    AnexoServico anexoServico;
    @EJB
    ConfiguracaoServico configuracaoServico;
    @EJB
    private JogoServico jogoServico;
    @EJB
    LoginServico loginServico;
    private Player player;
    private Player playerApagar;
    private Player playerPesquisar;
    private List<Player> players;
    private UploadedFile file;
    private String fileTemp;
    private StreamedContent imagem;
    private Configuracao configuracao;
    private List<Jogo> jogos;
    private List<Jogo> jogosSelecionados;
    private Jogo jogo;
    private boolean cadastrar;

    @PostConstruct
    public void init() {
        try {
            HttpServletRequest uri = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
            instanciar();

            String visualizarPlayerId = FacesUtil
                    .getRequestParameter("id");

            String cadastrarParam = FacesUtil
                    .getRequestParameter("cadastrar");

            if (cadastrarParam != null) {
                cadastrar = true;
            }

            String redirectSteam = FacesUtil
                    .getRequestParameter("redirectSteamLogin");

            if (redirectSteam != null && !redirectSteam.isEmpty()) {
                cadastrar = true;
                processSteamReturn();
            }

//            if (visualizarPlayerId == null && cadastrarParam == null && redirectSteam == null) {
//                Mensagem.errorAndRedirect("Você não tem permissão para acessar essa página!", "index.xhtml");
//            }
            if (visualizarPlayerId != null && !visualizarPlayerId.isEmpty()) {
                this.player = this.playerServico.buscaPlayer(Long.parseLong(visualizarPlayerId));
                if (this.player.getId() != null && this.player.getAnexo() != null) {
                    System.err.println("pegando avatar...");
                    this.fileTemp = this.player.getAnexo().getNome();
                }
            }

            if (uri.getRequestURI().contains("criarJogador.xhtml") && this.player.getId() == null) {
                this.jogos = jogoServico.pesquisar();
            }
        } catch (Exception ex) {
            System.err.println(ex);
        }

    }

    public void instanciar() {
        this.player = new Player();
        this.playerPesquisar = new Player();
        this.playerApagar = new Player();
        this.players = null;
        this.configuracao = new Configuracao();
        this.jogo = new Jogo();
        this.jogos = new ArrayList<>();
        this.jogosSelecionados = new ArrayList<>();
        this.fileTemp = null;
    }

    public Player getPlayerPesquisar() {
        return playerPesquisar;
    }

    public void setPlayerPesquisar(Player playerPesquisar) {
        this.playerPesquisar = playerPesquisar;
    }

    public Player getP() {
        return player;
    }

    public Player getPlayerApagar() {
        return playerApagar;
    }

    public void setPlayerApagar(Player playerApagar) {
        this.playerApagar = playerApagar;
    }

    public void setP(Player player) {
        this.player = player;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public void setPlayers(List<Player> players) {
        this.players = players;
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

    public Configuracao getConfiguracao() {
        return configuracao;
    }

    public void setConfiguracao(Configuracao configuracao) {
        this.configuracao = configuracao;
    }

    public void doUpload(FileUploadEvent event) {
        this.player.setAnexo(anexoServico.fileUploadTemp(event));
        this.fileTemp = this.player.getAnexo().getNome();
        System.err.println("file temp " + this.fileTemp);
        PrimeFaces.current().executeScript("atualizarImagem();");
    }

    public List<Jogo> getJogos() {
        return jogos;
    }

    public void setJogos(List<Jogo> jogos) {
        this.jogos = jogos;
    }

    public Jogo getJogo() {
        return jogo;
    }

    public void setJogo(Jogo jogo) {
        this.jogo = jogo;
    }

    public List<Jogo> getJogosSelecionados() {
        return jogosSelecionados;
    }

    public void setJogosSelecionados(List<Jogo> jogosSelecionados) {
        this.jogosSelecionados = jogosSelecionados;
    }

    public void salvarPlayer() throws Exception {

        this.player.setJogos(this.jogosSelecionados);
        if (this.player.getAnexo() != null && this.player.getAnexo().getId() == null) {
            this.player.setAnexo(anexoServico.salvarAnexo(this.player.getAnexo()));
        }
        if (this.player.getId() == null) {
            this.player = playerServico.registrarPlayer(this.player, null, Url.REGISTRAR_PLAYER.getNome());
            if (cadastrar) {
                LoginVo loginVo = new LoginVo();
                loginVo.setLogin(this.player.getLogin());
                loginVo.setSenha(this.player.getSenha());
                loginServico.autenticar(loginVo);

            }
            Mensagem.successAndRedirect("Player salvo com sucesso", "jogador.xhtml?id=" + this.player.getId());
        } else {
            this.player = playerServico.save(this.player, this.player.getId(), Url.ATUALIZAR_PLAYER.getNome());
            Mensagem.successAndRedirect("Player atualizado com sucesso", "jogador.xhtml?id=" + this.player.getId());
        }

    }

    public void pesquisarPlayer() throws Exception {
        if (this.playerPesquisar.getNome() == null) {
            this.playerPesquisar.setNome("");
        }
        this.players = playerServico.pesquisar(this.playerPesquisar.getNome());
    }

    public void limpar() {
        instanciar();
    }

    public void removerPlayer() throws Exception {
        this.playerServico.delete(this.playerApagar, Url.APAGAR_PLAYER.getNome());
        Mensagem.successAndRedirect("pesquisarPlayer.xhtml");
        init();
    }

    public void adicionarJogo() {
        this.jogosSelecionados.add(this.jogo);
        this.jogo = new Jogo();

    }

    public String getFileTemp() {
        return fileTemp;
    }

    public void setFileTemp(String fileTemp) {
        this.fileTemp = fileTemp;
    }

    public List<Jogo> jogos() {
        return jogos;
    }

    private void processSteamReturn() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) facesContext.getExternalContext().getRequest();

        Map<String, String[]> parameters = request.getParameterMap();
        String steamId = parameters.get("openid.claimed_id")[0];

        if (steamId != null && !steamId.isEmpty()) {
            try {

                String decodedSteamId = URLDecoder.decode(steamId, StandardCharsets.UTF_8.name());

                String steamId64 = decodedSteamId.substring(decodedSteamId.lastIndexOf("/") + 1);
                System.err.println("steamId64 " + steamId64);
                this.player.setSteamId64(steamId64);

                PlayerSteamVo playerVo = new PlayerSteamVo();
                playerVo = playerServico.getPlayerInfo(steamId64, "F10A919CE16995E066B463C9005AF4D3");

                if (playerVo.getSteamid() != null) {
                    LoginVo login = new LoginVo();
                    login.setSteamId(playerVo.getSteamid());
                    if (loginServico.autenticarSteam(login) != null) {

                        Mensagem.successAndRedirect("Login realizado com sucesso!", "index.xhtml");
                    }

                }

                if (playerVo.getPersonaname() != null) {
                    this.player.setNick(playerVo.getPersonaname());
                }

                if (playerVo.getRealname() != null) {
                    this.player.setNome(playerVo.getRealname());
                }

                if (playerVo.getSteamid() != null) {
                    this.player.setSteamID(playerVo.getSteamid());
                }

                if (playerVo.getProfileurl() != null) {
                    this.player.setUrlSteam(playerVo.getProfileurl());
                }

                if (playerVo.getAvatarfull() != null) {
                    try {
                        String imagemUrl = playerVo.getAvatarfull();
                        String destino = "/tmp";
                        String nomeArquivo = "steam_avatar_" + System.currentTimeMillis() + ".jpg";

                        Files.createDirectories(Paths.get(destino));

                        File arquivoDestino = new File(destino, nomeArquivo);
                        desabilitaVerificacaoSSL();

                        try (InputStream in = new URL(imagemUrl).openStream(); OutputStream out = new FileOutputStream(arquivoDestino)) {

                            byte[] buffer = new byte[8192];
                            int bytesLidos;
                            while ((bytesLidos = in.read(buffer)) != -1) {
                                out.write(buffer, 0, bytesLidos);
                            }

                            String caminhoFinal = arquivoDestino.getAbsolutePath();

                            System.err.println("caminho final imagem steam " + caminhoFinal);
                            this.fileTemp = caminhoFinal;
                            if (this.fileTemp != null) {
                                PrimeFaces.current().executeScript("atualizarImagem();");

                            }
                            Anexo anexo = new Anexo();
                            anexo.setNomeExibicao(nomeArquivo);
                            anexo.setNome(caminhoFinal);

                            this.player.setAnexo(anexo);

                        } catch (IOException e) {
                            System.err.println("Erro ao baixar imagem: " + e.getMessage());
                        }

                    } catch (Exception ex) {
                        System.err.println("Erro geral ao processar avatar: " + ex.getMessage());
                    }

                }

            } catch (Exception e) {
                e.printStackTrace();
                Mensagem.error("Erro ao processar o retorno da Steam.");
            }
        } else {
            Mensagem.error("Erro ao processar o retorno da Steam.");
        }

    }

    public static void desabilitaVerificacaoSSL() throws Exception {
        TrustManager[] trustAllCerts = new TrustManager[]{
            new X509TrustManager() {
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return null;
                }

                public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
                }

                public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
                }
            }
        };

        SSLContext sc = SSLContext.getInstance("SSL");
        sc.init(null, trustAllCerts, new java.security.SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

        // Ignora hostname
        HostnameVerifier allHostsValid = (hostname, session) -> true;
        HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
    }

    public void removerFoto() {
        this.player.setAnexo(null);
        this.fileTemp = null;
        PrimeFaces.current().executeScript("atualizarImagem();");

    }
}
