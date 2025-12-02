/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.champ.Utilitario;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.application.FacesMessage.Severity;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 *
 * @author andre
 */
public class Mensagem {

    public static final String SUCCESS = "Operação Realizada com Sucesso!";

    public static final String ERROR = "Erro ao Realizar a Operação!";

    public static final String WARN = "Falha ao Realizar a Operação!";

    public static final String FATAL = "Erro Grave ao Realizar a Operação!";

    public static final String PERMISSAO_NEGADA = "Você não tem permissão para executar esta Operação!";

    public static void success() {
        adicionarMensagemSuccess(SUCCESS, null);
    }

    public static void success(String mensagem) {
        adicionarMensagemSuccess(mensagem, null);
    }

    public static void success(String mensagem, String detalhes) {
        adicionarMensagemSuccess(mensagem, detalhes);
    }

    public static void successAndRedirect(String url) {
        adicionarMensagemSuccess(SUCCESS, null);
        redirect(url);
    }

    public static void successAndRedirect(String mensagem, String url) {
        adicionarMensagemSuccess(mensagem, null);
        redirect(url);
    }

    public static void successAndRedirect(String mensagem, String detalhes, String url) {
        adicionarMensagemSuccess(mensagem, detalhes);
        redirect(url);
    }

    public static void error() {
        adicionarMensagemError(ERROR, null);
    }

    public static void error(String mensagem) {
        adicionarMensagemError(mensagem, null);
    }

    public static void error(String mensagem, String detalhes) {
        adicionarMensagemError(mensagem, detalhes);
    }

    public static void errorAndRedirect(String url) {
        adicionarMensagemError(SUCCESS, null);
        redirect(url);
    }

    public static void errorAndRedirect(String mensagem, String url) {
        adicionarMensagemError(mensagem, null);
        redirect(url);
    }

    public static void errorAndRedirect(String mensagem, String detalhes, String url) {
        adicionarMensagemError(mensagem, detalhes);
        redirect(url);
    }

    public static void warn() {
        adicionarMensagemWarn(WARN, null);
    }

    public static void warn(String mensagem) {
        adicionarMensagemWarn(mensagem, null);
    }

    public static void warn(String mensagem, String detalhes) {
        adicionarMensagemWarn(mensagem, detalhes);
    }

    public static void warnAndRedirect(String url) {
        adicionarMensagemWarn(SUCCESS, null);
        redirect(url);
    }

    public static void warnAndRedirect(String mensagem, String url) {
        adicionarMensagemWarn(mensagem, null);
        redirect(url);
    }

    public static void warnAndRedirect(String mensagem, String detalhes, String url) {
        adicionarMensagemWarn(mensagem, detalhes);
        redirect(url);
    }

    public static void fatal() {
        adicionarMensagemFatal(FATAL, null);
    }

    public static void fatal(String mensagem) {
        adicionarMensagemFatal(mensagem, null);
    }

    public static void fatal(String mensagem, String detalhes) {
        adicionarMensagemFatal(mensagem, detalhes);
    }

    public static void fatalAndRedirect(String url) {
        adicionarMensagemFatal(FATAL, null);
        redirect(url);
    }

    public static void fatalAndRedirect(String mensagem, String url) {
        adicionarMensagemFatal(mensagem, null);
        redirect(url);
    }

    public static void fatalAndRedirect(String mensagem, String detalhes, String url) {
        adicionarMensagemFatal(mensagem, detalhes);
        redirect(url);
    }

    public static void permissaoNegada() {
        adicionarMensagemPermissaoNegada(PERMISSAO_NEGADA, null);
    }

    public static void permissaoNegada(String mensagem) {
        adicionarMensagemPermissaoNegada(mensagem, null);
    }

    public static void permissaoNegada(String mensagem, String detalhes) {
        adicionarMensagemPermissaoNegada(mensagem, detalhes);
    }

    public static void permissaoNegadaAndRedirect(String url) {
        adicionarMensagemPermissaoNegada(PERMISSAO_NEGADA, null);
        redirect(url);
    }

    public static void permissaoNegadaAndRedirect(String mensagem, String url) {
        adicionarMensagemPermissaoNegada(mensagem, null);
        redirect(url);
    }

    public static void permissaoNegadaAndRedirect(String mensagem, String detalhes, String url) {
        adicionarMensagemPermissaoNegada(mensagem, detalhes);
        redirect(url);
    }

    private static void adicionarMensagemSuccess(String summary, String detail) {
        adicionarMensagem(FacesMessage.SEVERITY_INFO, summary, detail);
    }

    private static void adicionarMensagemError(String summary, String detail) {
        adicionarMensagem(FacesMessage.SEVERITY_ERROR, summary, detail);
    }

    private static void adicionarMensagemWarn(String summary, String detail) {
        adicionarMensagem(FacesMessage.SEVERITY_WARN, summary, detail);
    }

    private static void adicionarMensagemFatal(String summary, String detail) {
        adicionarMensagem(FacesMessage.SEVERITY_FATAL, summary, detail);
    }

    private static void adicionarMensagemPermissaoNegada(String summary, String detail) {
        adicionarMensagem(FacesMessage.SEVERITY_ERROR, summary, detail);
    }

    private static void adicionarMensagem(Severity severiry, String summary, String detail) {
        FacesUtil.getFacesContext()
                .addMessage(null, new FacesMessage(severiry, summary, detail));
    }

    public static void onlyRedirect(String url) {
        redirect(url);
    }

    private static void redirect(String url) {

        try {

            manterMensagem();

            FacesUtil.getFacesContext()
                    .getExternalContext()
                    .redirect(url);

        } catch (IOException ex) {
            Logger.getLogger(Mensagem.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private static void manterMensagem() {
        FacesUtil.getFacesContext()
                .getExternalContext()
                .getFlash()
                .setKeepMessages(true);
    }

}
