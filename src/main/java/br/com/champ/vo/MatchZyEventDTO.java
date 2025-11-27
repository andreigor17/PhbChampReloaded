/*
 * DTO base para eventos do MatchZy
 * Estrutura baseada na documentação OpenAPI do MatchZy
 */
package br.com.champ.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import java.util.Map;

/**
 * DTO genérico para receber eventos do MatchZy
 * O MatchZy envia eventos em formato JSON via HTTP POST
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class MatchZyEventDTO implements Serializable {
    
    private String event; // Tipo do evento: match_start, round_start, round_end, kill, bomb_planted, etc.
    private String match_id; // ID da partida
    private Long timestamp; // Timestamp do evento
    private Map<String, Object> data; // Dados específicos do evento
    
    // Campos comuns em vários eventos
    private String map_name;
    private Integer round_number;
    private Integer score_ct;
    private Integer score_t;
    
    public MatchZyEventDTO() {
    }
    
    public String getEvent() {
        return event;
    }
    
    public void setEvent(String event) {
        this.event = event;
    }
    
    public String getMatch_id() {
        return match_id;
    }
    
    public void setMatch_id(String match_id) {
        this.match_id = match_id;
    }
    
    public Long getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }
    
    public Map<String, Object> getData() {
        return data;
    }
    
    public void setData(Map<String, Object> data) {
        this.data = data;
    }
    
    public String getMap_name() {
        return map_name;
    }
    
    public void setMap_name(String map_name) {
        this.map_name = map_name;
    }
    
    public Integer getRound_number() {
        return round_number;
    }
    
    public void setRound_number(Integer round_number) {
        this.round_number = round_number;
    }
    
    public Integer getScore_ct() {
        return score_ct;
    }
    
    public void setScore_ct(Integer score_ct) {
        this.score_ct = score_ct;
    }
    
    public Integer getScore_t() {
        return score_t;
    }
    
    public void setScore_t(Integer score_t) {
        this.score_t = score_t;
    }
}

