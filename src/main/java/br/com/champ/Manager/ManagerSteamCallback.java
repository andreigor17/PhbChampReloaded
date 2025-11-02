package br.com.champ.Manager;

import br.com.champ.Servico.LoginServico;
import br.com.champ.Servico.PlayerServico;
import br.com.champ.Utilitario.Mensagem;
import br.com.champ.vo.LoginVo;
import br.com.champ.vo.PlayerSteamVo;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.Serializable;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 *
 * @author andre
 */
@RequestScoped
@Named
public class ManagerSteamCallback implements Serializable{

    @Inject
    LoginServico loginServico;
    
    @EJB
    PlayerServico playerServico;
    
    @Inject
    ExternalContext externalContext;
    
    @PostConstruct
    public void init() {
        System.err.println("=== ManagerSteamCallback.init() foi chamado ===");
        try {
            HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
            Map<String, String[]> parameters = request.getParameterMap();
            System.err.println("Parâmetros recebidos: " + parameters.keySet());

            // Validação de parâmetros da Steam
            if (!parameters.containsKey("openid.claimed_id") || parameters.get("openid.claimed_id") == null 
                    || parameters.get("openid.claimed_id").length == 0) {
                System.err.println("Erro: parâmetro openid.claimed_id não encontrado");
                Mensagem.error("Erro ao processar o retorno da Steam. Parâmetros inválidos.");
                redirectToIndex();
                return;
            }

            String steamId = parameters.get("openid.claimed_id")[0];
            if (steamId == null || steamId.trim().isEmpty()) {
                System.err.println("Erro: steamId vazio");
                Mensagem.error("Erro ao processar o retorno da Steam. ID inválido.");
                redirectToIndex();
                return;
            }

            // Processa o Steam ID
            String decodedSteamId = URLDecoder.decode(steamId, StandardCharsets.UTF_8.name());
            String steamId64 = decodedSteamId.substring(decodedSteamId.lastIndexOf("/") + 1);
            
            // Valida o formato do Steam ID (deve ser numérico)
            if (!steamId64.matches("\\d+")) {
                System.err.println("Erro: Steam ID inválido - " + steamId64);
                Mensagem.error("Erro ao processar o retorno da Steam. ID inválido.");
                redirectToIndex();
                return;
            }

            System.err.println("Processando Steam ID: " + steamId64);

            // Busca informações do player na Steam
            PlayerSteamVo playerVo = playerServico.getPlayerInfo(steamId64, "F10A919CE16995E066B463C9005AF4D3");

            if (playerVo == null || playerVo.getSteamid() == null) {
                System.err.println("Erro: não foi possível obter dados da Steam para ID: " + steamId64);
                Mensagem.error("Não foi possível obter informações da Steam. Tente novamente.");
                redirectToIndex();
                return;
            }

            // TENTA FAZER LOGIN primeiro - se já tem conta, redireciona para index
            LoginVo login = new LoginVo();
            login.setSteamId(playerVo.getSteamid());
            String tokenResult = loginServico.autenticarSteam(login);
            
            if (tokenResult != null && !tokenResult.trim().isEmpty()) {
                // Login bem-sucedido - já tem conta
                System.err.println("Login Steam bem-sucedido. Redirecionando para index...");
                redirectToIndex();
                return;
            }

            // SE NÃO CONSEGUIU LOGAR - é um novo usuário, redireciona para cadastro
            System.err.println("Novo usuário Steam. Redirecionando para cadastro...");
            redirectToCadastro();
            
        } catch (Exception e) {
            System.err.println("Erro ao processar retorno da Steam: " + e.getMessage());
            e.printStackTrace();
            Mensagem.error("Erro ao processar o retorno da Steam. Tente novamente.");
            redirectToIndex();
        }
    }
    
    private void redirectToIndex() {
        try {
            System.err.println("Redirecionando para index.xhtml...");
            externalContext.redirect("index.xhtml");
            FacesContext.getCurrentInstance().responseComplete();
        } catch (IOException e) {
            System.err.println("Erro ao redirecionar para index: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void redirectToCadastro() {
        try {
            System.err.println("Redirecionando para criarJogador.xhtml...");
            externalContext.redirect("criarJogador.xhtml?cadastrar=true");
            FacesContext.getCurrentInstance().responseComplete();
        } catch (IOException e) {
            System.err.println("Erro ao redirecionar para cadastro: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

