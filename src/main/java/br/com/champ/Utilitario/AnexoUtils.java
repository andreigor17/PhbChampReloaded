/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.champ.Utilitario;

import java.io.File;
import java.io.FileOutputStream;
import java.sql.Timestamp;
import org.primefaces.event.FileUploadEvent;

/**
 *
 * @author andre
 */
public class AnexoUtils {

    private static String diretorio;
    private static String caminho;
    private static byte[] arquivo;
    private static String nome;
    private static String REAL_PATH_OPT = "/opt/uploads";
    private static String pathWindows = "D:\\apipath.json";

    public static String getNome() {
        return nome;
    }

    public static void fileUpload(FileUploadEvent event, String type, String diretorio) {
        try {
            Timestamp playerAvatar = new Timestamp(System.currentTimeMillis());
            nome = playerAvatar + type;
            caminho = REAL_PATH_OPT + diretorio + getNome();
            arquivo = event.getFile().getContent();

            File file = new File(REAL_PATH_OPT + diretorio);
            file.mkdirs();

        } catch (Exception ex) {
            System.out.println("Erro no upload do arquivo" + ex);
        }
    }

    public void gravar() {

        try {

            FileOutputStream fos;
            fos = new FileOutputStream(this.caminho);
            fos.write(this.arquivo);
            System.out.println("caminho do arquivo " + this.caminho);
            fos.close();

        } catch (Exception ex) {
            System.out.println(ex);
        }

    }

}
