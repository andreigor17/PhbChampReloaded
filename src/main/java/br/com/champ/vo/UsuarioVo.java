/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package br.com.champ.vo;

import br.com.champ.Enums.UserRoles;

/**
 *
 * @author andrepc
 */
public class UsuarioVo {
    
    
    private String login;
    private String senha;
    private UserRoles role;
    private boolean adminastror;
    private String nome;
    private String nick;

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public UserRoles getRole() {
        return role;
    }

    public void setRole(UserRoles role) {
        this.role = role;
    }

    public boolean isAdminastror() {
        return adminastror;
    }

    public void setAdminastror(boolean adminastror) {
        this.adminastror = adminastror;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getNick() {
        return nick;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }
    
    
    
    
}
