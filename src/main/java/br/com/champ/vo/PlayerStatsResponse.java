/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package br.com.champ.vo;

import java.util.List;

/**
 *
 * @author andrepc
 */
public class PlayerStatsResponse {
    
    private PlayerStats playerstats;

    public PlayerStatsResponse() {
    }

    public PlayerStats getPlayerstats() {
        return playerstats;
    }

    public void setPlayerstats(PlayerStats playerstats) {
        this.playerstats = playerstats;
    }

    @Override
    public String toString() {
        return "PlayerStatsResponse{" +
                "playerstats=" + playerstats +
                '}';
    }
}

/**
 * Representa as estatísticas do jogador.
 */
class PlayerStats {
    
    private String steamID;
    private String gameName;
    private List<Stat> stats;
    private List<Achievement> achievements;

    public PlayerStats() {
    }

    public String getSteamID() {
        return steamID;
    }

    public void setSteamID(String steamID) {
        this.steamID = steamID;
    }

    public String getGameName() {
        return gameName;
    }

    public void setGameName(String gameName) {
        this.gameName = gameName;
    }

    public List<Stat> getStats() {
        return stats;
    }

    public void setStats(List<Stat> stats) {
        this.stats = stats;
    }

    public List<Achievement> getAchievements() {
        return achievements;
    }

    public void setAchievements(List<Achievement> achievements) {
        this.achievements = achievements;
    }

    @Override
    public String toString() {
        return "PlayerStats{" +
                "steamID='" + steamID + '\'' +
                ", gameName='" + gameName + '\'' +
                ", stats=" + stats +
                ", achievements=" + achievements +
                '}';
    }
}

/**
 * Representa cada estatística individual (por exemplo, total_kills, total_deaths, etc).
 */
class Stat {
    
    private String name;
    private int value;

    public Stat() {
    }

    public Stat(String name, int value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "Stat{" +
                "name='" + name + '\'' +
                ", value=" + value +
                '}';
    }
}

/**
 * Representa cada conquista (achievement) do jogador.
 */
class Achievement {
    
    private String name;
    private int achieved; // 1 para alcançado, 0 para não alcançado

    public Achievement() {
    }

    public Achievement(String name, int achieved) {
        this.name = name;
        this.achieved = achieved;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAchieved() {
        return achieved;
    }

    public void setAchieved(int achieved) {
        this.achieved = achieved;
    }

    @Override
    public String toString() {
        return "Achievement{" +
                "name='" + name + '\'' +
                ", achieved=" + achieved +
                '}';
    }
}
