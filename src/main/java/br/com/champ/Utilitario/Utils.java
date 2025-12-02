/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package br.com.champ.Utilitario;

import java.util.Collection;

/**
 *
 * @author andreigor
 */
public class Utils {
    
    public static String cpfMask = "###.###.###-##";
    public static String cnpjMask = "##.###.###/####-##";
    public static String cepMask = "##.###-###";
    public static String phoneMask = "(##) ####-####";
    public static String simplePhoneMask = "####-####";
    public static String newSimplePhoneMask = "#####-####";
    public static String newPhoneMask = "(##) #####-####";

    public static String removerAcentuacao(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("¦", "").replaceAll("[ãâàáä]", "a").replaceAll("[êèéë]", "e").replaceAll("[îìíï]", "i")
                .replaceAll("[õôòóö]", "o").replaceAll("[ûúùü]", "u").replaceAll("[ÃÂÀÁÄ]", "A")
                .replaceAll("[ÊÈÉË]", "E").replaceAll("[ÎÌÍÏ]", "I").replaceAll("[ÕÔÒÓÖ]", "O")
                .replaceAll("[ÛÙÚÜ]", "U").replace('ç', 'c').replace('Ç', 'C').replace('ñ', 'n').replace('Ñ', 'N')
                .replace((char) 160, (char) 32);
    }

    public static String removeCaracteresEspeciais(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("¦", "").replaceAll("-", "").replaceAll("[ãâàáä]", "a").replaceAll("[êèéë]", "e")
                .replaceAll("[îìíï]", "i").replaceAll("[õôòóö]", "o").replaceAll("[ûúùü]", "u")
                .replaceAll("[ÃÂÀÁÄ]", "A").replaceAll("[ÊÈÉË]", "E").replaceAll("[ÎÌÍÏ]", "I")
                .replaceAll("[ÕÔÒÓÖ]", "O").replaceAll("[ÛÙÚÜ]", "U").replace('ç', 'c').replace('Ç', 'C')
                .replace('ñ', 'n').replace('Ñ', 'N').replaceAll("!", "")
                .replaceAll("\\[\\´\\`\\?!\\@\\#\\$\\%\\¨\\*", " ").replaceAll("\\(\\)\\=\\{\\}\\[\\]\\~\\^\\]", " ")
                .replaceAll("[\\.\\;\\-\\_\\+\\'\\ª\\º\\:\\;\\/\\°]", " ").replace((char) 160, (char) 32)
                .replace("\"", "");
    }
    
    // public static String codificarSenha(String senha) {
    //     return new BCryptPasswordEncoder().encode(senha);
    // }

    public static boolean isEmpty(Collection collection) {
        return collection == null || collection.isEmpty();
    }

    public static boolean isNotEmpty(Collection collection) {
        return !isEmpty(collection);
    }

    public static boolean isEmpty(String string) {
        return string == null || string.length() == 0;
    }

    public static boolean isNotEmpty(String string) {
        return !isEmpty(string);
    }

    /**
     * <p>
     * É considerado vazio todo e qualquer array cujo valor seja
     * <b>null</b>, ou o tamanho seja <b>zero</b>, ou todos os elementos
     * contidos no array sejam <b>null</b>.
     * </p>
     *
     * @param args
     * @return
     */
    public static boolean isEmpty(Object... args) {

        // Qualquer valor passado que seja nulo sera considerado como vazio
        if (args == null || args.length == 0) {
            return true;
        } else {
            // pecorre o array, se existe um item não nulo é porque o array não esta vazio
            for (Object object : args) {
                if (object != null) {
                    return false;
                }
            }
            return true;
        }
    }

    public static boolean isNotEmpty(Object... args) {
        return !isEmpty(args);
    }
    
}
