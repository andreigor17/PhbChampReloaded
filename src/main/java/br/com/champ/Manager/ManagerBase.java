/*
 * Classe base para Managers que precisam de acesso ao player logado
 */
package br.com.champ.Manager;

import br.com.champ.Modelo.Player;
import br.com.champ.Servico.LoginServico;
import jakarta.inject.Inject;
import java.io.Serializable;

/**
 * Classe base abstrata para managers que necessitam acessar o usuário logado
 * Fornece funcionalidades comuns como obter player logado e verificar permissões
 * 
 * @author PhbChamp Team
 */
public abstract class ManagerBase implements Serializable {

    @Inject
    protected LoginServico loginServico;
    
    protected Player playerLogado;

    /**
     * Obtém o player logado da sessão
     * @return Player logado ou null se não houver usuário logado
     */
    public Player getPlayerLogado() {
        if (playerLogado == null) {
            try {
                playerLogado = loginServico.obterPlayerId();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return playerLogado;
    }

    /**
     * Define o player logado
     * @param playerLogado Player logado
     */
    public void setPlayerLogado(Player playerLogado) {
        this.playerLogado = playerLogado;
    }

    /**
     * Verifica se há um usuário logado
     * @return true se há usuário logado, false caso contrário
     */
    public boolean isUsuarioLogado() {
        try {
            return loginServico.usuarioEstaLogado();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Verifica se o usuário logado é administrador
     * @return true se o usuário logado é admin, false caso contrário
     */
    public boolean isAdmin() {
        Player player = getPlayerLogado();
        return player != null && player.isAdminastror();
    }

    /**
     * Verifica se o usuário logado é o dono do recurso (mesmo ID)
     * @param resourcePlayerId ID do player dono do recurso
     * @return true se o usuário logado é o dono ou é admin, false caso contrário
     */
    public boolean isOwnerOrAdmin(Long resourcePlayerId) {
        if (resourcePlayerId == null) {
            return false;
        }
        Player player = getPlayerLogado();
        if (player == null) {
            return false;
        }
        // É admin ou é o próprio dono
        return player.isAdminastror() || (player.getId() != null && player.getId().equals(resourcePlayerId));
    }

    /**
     * Verifica se o usuário logado pode editar o recurso
     * Útil para verificar permissões de edição/exclusão
     * @param resourcePlayerId ID do player dono do recurso
     * @return true se pode editar, false caso contrário
     */
    public boolean podeEditar(Long resourcePlayerId) {
        return isOwnerOrAdmin(resourcePlayerId);
    }
}

