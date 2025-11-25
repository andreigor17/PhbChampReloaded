/*
 * Value Object para informações de jogador em tempo real
 */
package br.com.champ.vo;

import java.io.Serializable;

/**
 * Informações de um jogador durante a partida
 */
public class PlayerInfo implements Serializable {
    
    private String nome;
    private String steamId;
    private int time; // 2 = CT, 3 = T
    private int health;
    private int armor;
    private int kills;
    private int deaths;
    private int assists;
    private boolean alive;
    private String weapon;
    
    public PlayerInfo() {
    }
    
    public String getNome() {
        return nome;
    }
    
    public void setNome(String nome) {
        this.nome = nome;
    }
    
    public String getSteamId() {
        return steamId;
    }
    
    public void setSteamId(String steamId) {
        this.steamId = steamId;
    }
    
    public int getTime() {
        return time;
    }
    
    public void setTime(int time) {
        this.time = time;
    }
    
    public String getTimeNome() {
        return time == 2 ? "CT" : (time == 3 ? "T" : "ESPECTADOR");
    }
    
    public int getHealth() {
        return health;
    }
    
    public void setHealth(int health) {
        this.health = health;
    }
    
    public int getArmor() {
        return armor;
    }
    
    public void setArmor(int armor) {
        this.armor = armor;
    }
    
    public int getKills() {
        return kills;
    }
    
    public void setKills(int kills) {
        this.kills = kills;
    }
    
    public int getDeaths() {
        return deaths;
    }
    
    public void setDeaths(int deaths) {
        this.deaths = deaths;
    }
    
    public int getAssists() {
        return assists;
    }
    
    public void setAssists(int assists) {
        this.assists = assists;
    }
    
    public boolean isAlive() {
        return alive;
    }
    
    public void setAlive(boolean alive) {
        this.alive = alive;
    }
    
    public String getWeapon() {
        return weapon;
    }
    
    public void setWeapon(String weapon) {
        this.weapon = weapon;
    }
}

