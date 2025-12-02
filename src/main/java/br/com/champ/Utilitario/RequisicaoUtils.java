/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package br.com.champ.Utilitario;

import java.util.concurrent.TimeUnit;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 *
 * @author andre
 */
public class RequisicaoUtils {

    /**
     * Método de requisição POST que lança exceção para erros de autenticação
     * @param url URL do endpoint
     * @param json JSON a ser enviado
     * @return String com a resposta
     * @throws AuthenticationException quando o código de status é 401
     */
    public static String requisicaoPostComAuth(String url, String json) throws AuthenticationException {
        try {

            OkHttpClient client = new OkHttpClient().newBuilder()
                    .connectTimeout(1, TimeUnit.MINUTES)
                    .writeTimeout(1, TimeUnit.MINUTES)
                    .readTimeout(1, TimeUnit.MINUTES).build();

            MediaType mediaType = MediaType.parse("application/json");
            RequestBody body = RequestBody.create(mediaType, json);
            System.err.println("body " + json);
            System.err.println("url " + url);
            Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .addHeader("Content-Type", "application/json")
                    .build();

            try (Response response = client.newCall(request).execute()) {
                int statusCode = response.code();
                
                // Verifica se é erro 401 (Unauthorized)
                if (statusCode == 401) {
                    throw new AuthenticationException("Credenciais inválidas ou usuário não encontrado", 401);
                }
                
                if (!response.isSuccessful()) {
                    System.out.println("Erro inesperado: " + response);
                    return null;
                } 

                return response.body().string();
            }
        } catch (AuthenticationException e) {
            // Repassa a exceção de autenticação
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String requisicaoPost(String url, String json) {
        try {

            OkHttpClient client = new OkHttpClient().newBuilder()
                    .connectTimeout(1, TimeUnit.MINUTES)
                    .writeTimeout(1, TimeUnit.MINUTES)
                    .readTimeout(1, TimeUnit.MINUTES).build();

            MediaType mediaType = MediaType.parse("application/json");
            RequestBody body = RequestBody.create(mediaType, json);
            System.err.println("body " + json);
            System.err.println("url " + url);
            Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .addHeader("Content-Type", "application/json")
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    //System.out.println("Resposta: " + response.body().string());
                    System.out.println("Erro inesperado: " + response);
                    return null;
                } 

                return response.body().string();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String requisicaoGet(String url) {
        try {

            OkHttpClient client = new OkHttpClient().newBuilder()
                    .connectTimeout(1, TimeUnit.MINUTES)
                    .writeTimeout(1, TimeUnit.MINUTES)
                    .readTimeout(1, TimeUnit.MINUTES).build();

            Request request = new Request.Builder()
                    .url(url)
                    .get()
                    .addHeader("Content-Type", "text/plain;charset=UTF-8")
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    System.out.println("Resposta: " + response.body().string());
                    System.out.println("Erro inesperado: " + response);
                    return null;
                }

                return response.body().string();
            }

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String requisicaoGetToken(String url, String token) {
        try {

            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(url)
                    .get()
                    .addHeader("Content-Type", "text/plain;charset=UTF-8")
                    .addHeader("Authorization", "Bearer " + token)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    System.out.println("Resposta: " + response.body().string());
                    System.out.println("Erro inesperado: " + response);
                    return null;
                }

                return response.body().string();
            }

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String requisicaoPostToken(String url, String json, String token) {
        try {

            OkHttpClient client = new OkHttpClient().newBuilder()
                    .connectTimeout(1, TimeUnit.MINUTES)
                    .writeTimeout(1, TimeUnit.MINUTES)
                    .readTimeout(1, TimeUnit.MINUTES).build();

            MediaType mediaType = MediaType.parse("application/json");
            RequestBody body = RequestBody.create(mediaType, json);

            Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Authorization", "Bearer " + token)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    System.out.println("Resposta: " + response.body().string());
                    System.out.println("Erro inesperado: " + response);
                    return null;
                }

                return response.body().string();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
