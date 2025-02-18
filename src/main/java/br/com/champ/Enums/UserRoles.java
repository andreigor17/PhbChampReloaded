/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Enum.java to edit this template
 */
package br.com.champ.Enums;

/**
 *
 * @author andrepc
 */
public enum UserRoles {

    ADMIN("Admin"),
    USER("User");

    private String role;

    private UserRoles(String role) {
        this.role = role;
    }

}
