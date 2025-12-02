/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Enum.java to edit this template
 */
package br.com.champ.Enums;

/**
 *
 * @author andre
 */
public enum Cs2ConsoleCommand {
    // ===============================
    // 🎮 Comandos Básicos / Gerais
    SV_CHEATS("sv_cheats 1 / 0", "Permite ou desabilita comandos de cheat (necessário para muitos comandos)."),
    QUIT("quit", "Fecha o jogo ou servidor."),
    DISCONNECT("disconnect", "Desconecta-se do servidor atual."),
    CLEAR("clear", "Limpa o texto da console."),
    HELP("help <comando>", "Mostra descrição / ajuda de um comando específico."),
    FIND("find <texto>", "Busca comandos / cvars que contenham o texto fornecido."),

    // ===============================
    // 📊 Comandos de desempenho / HUD / gráficos
    CL_SHOWFPS("cl_showfps <0/1/2/3>", "Mostra o FPS (e informações adicionais nos níveis 2 e 3)."),
    FPS_MAX("fps_max <valor>", "Define limite máximo de frames por segundo (0 = sem limite)."),
    CQ_NETGRAPH("cq_netgraph <0/1>", "Mostra estatísticas de rede e FPS (equivalente ao net_graph)."),
    R_SHOW_BUILD_INFO("r_show_build_info <0/1>", "Mostra ou oculta a marca de versão / build na tela."),
    CL_DRAWHUD("cl_drawhud <0/1>", "Habilita ou desabilita elementos do HUD (apenas com cheats)."),
    CL_DRAW_ONLY_DEATHNOTICES("cl_draw_only_deathnotices <0/1>", "Desativa quase todo HUD, mantendo notificações de mortes."),

    // ===============================
    // 🧭 Comandos de mira / crosshair / HUD
    CL_CROSSHAIRSTYLE("cl_crosshairstyle <estilo>", "Define estilo da mira (ex: estática, dinâmica etc.)."),
    CL_CROSSHAIR_SIZE("cl_crosshairsize <valor>", "Define o tamanho da mira."),
    CL_CROSSHAIR_THICKNESS("cl_crosshairthickness <valor>", "Define a espessura das linhas da mira."),
    CL_CROSSHAIR_GAP("cl_crosshairgap <valor>", "Define o espaçamento entre as linhas da mira."),
    CL_CROSSHAIR_COLOR("cl_crosshaircolor <valor>", "Define cor da mira (1,2,3,4,5)."),
    CL_CROSSHAIR_T("cl_crosshair_t <0/1>", "Habilita ou não a parte superior da mira (modo T-style)."),
    CL_CROSSHAIR_DRAWOUTLINE("cl_crosshair_drawoutline <0/1>", "Ativa contorno na mira."),
    CL_CROSSHAIR_OUTLINETHICKNESS("cl_crosshair_outlinethickness <valor>", "Espessura do contorno da mira."),

    // ===============================
    // 🚀 Comandos de prática / cheats / utilitários
    NOCLIP("noclip", "Permite voar / atravessar paredes (requer sv_cheats)."),
    GOD("god", "Modo invencível (requer sv_cheats)."),
    KILL("kill", "Mata seu jogador (apenas para testes)."),
    SV_INFINITE_AMMO("sv_infinite_ammo <0/1>", "Dá munição infinita (requer sv_cheats)."),
    SV_RETHROW_LAST_GRENADE("sv_rethrow_last_grenade", "Refaz o último lançamento de granada (requer cheats)."),
    SV_SHOWIMPACTS("sv_showimpacts <0/1>", "Mostra os impactos das balas nas superfícies (requer cheats)."),
    SV_SHOWBULLETHITS("sv_showbullethits <0/1>", "Mostra onde a bala acertou o jogador (requer cheats)."),
    GIVE_WEAPON("give weapon_<nome>", "Dá uma arma ao jogador (requer sv_cheats)."),

    // ===============================
    // 🤖 Comandos de bots / equilíbrio / partida
    BOT_ADD("bot_add <CT/T> <dificuldade> <nome>", "Adiciona um bot ao time especificado."),
    BOT_ADD_CT("bot_add_ct <dificuldade> <nome>", "Adiciona um bot ao time CT."),
    BOT_ADD_T("bot_add_t <dificuldade> <nome>", "Adiciona um bot ao time T."),
    BOT_KICK("bot_kick", "Remove todos os bots da partida."),
    BOT_PLACE("bot_place", "Coloca um bot na posição apontada pela mira."),
    BOT_STOP("bot_stop <0/1>", "Faz os bots pararem ou retomarem movimento."),
    BOT_CROUCH("bot_crouch <0/1>", "Faz os bots ficarem agachados (útil para treinamento)."),
    BOT_MIMIC("bot_mimic <0/1>", "Faz os bots imitarem seus movimentos (quando ativo)."),
    MP_AUTOTEAMBALANCE("mp_autoteambalance <0/1>", "Habilita / desabilita equilíbrio automático entre times."),

    MP_RESTARTGAME("mp_restartgame <valor>", "Reinicia a partida depois do número de segundos especificado."),
    MP_ROUNDTIME("mp_roundtime <minutos>", "Define duração das rodadas (modo geral)."),
    MP_ROUNDTIME_DEFUSE("mp_roundtime_defuse <minutos>", "Define duração de rodadas em mapas de desarme."),
    MP_MAXROUNDS("mp_maxrounds <valor>", "Define número máximo de rodadas no mapa."),
    MP_TIMELIMIT("mp_timelimit <minutos>", "Define tempo máximo de partida em minutos."),
    MP_C4TIMER("mp_c4timer <segundos>", "Define tempo de explosão da bomba C4."),
    MP_BUY_ANYWHERE("mp_buy_anywhere <0/1>", "Permite comprar armas em qualquer local do mapa."),
    MP_STARTMONEY("mp_startmoney <valor>", "Define o dinheiro inicial dos jogadores."),
    MP_MAXMONEY("mp_maxmoney <valor>", "Define a quantia máxima de dinheiro possível."),
    MP_FREEZETIME("mp_freezetime <segundos>", "Define tempo de congelamento no início da rodada."),

    // ===============================
    // 🔧 Comandos de servidor / administração
    RCON_PASSWORD("rcon_password <senha>", "Define a senha para administração remota via RCON."),
    SV_VISIBLEMAXPLAYERS("sv_visiblemaxplayers <valor>", "Define número máximo visível de jogadores no mapa."),
    SV_SPECNOCLIP("sv_specnoclip <0/1>", "Permite espectadores voarem através de objetos."),
    SV_SPECSPEED("sv_specspeed <valor>", "Define velocidade de movimento dos espectadores."),

    // ===============================
    // 🌍 Comandos de mapa / nível
    MAP("map <nome_mapa>", "Muda o mapa para o especificado."),
    CHANGELEVEL("changelevel <nome_mapa>", "Troca de nível / mapa imediatamente."),
    LAUNCH_WARMUP_MAP("launch_warmup_map <nome_mapa>", "Inicia um mapa no modo warmup."),
    
    // ===============================
    // 🗣 Controle de voz / som
    VOICE_MUTE("voice_mute <nome|all>", "Silencia um jogador ou todos."),
    VOICE_UNMUTE("voice_unmute <nome|all>", "Remove silêncio de um jogador ou todos."),
    VOICE_SCALE("voice_scale <0.0-1.0>", "Define volume de voz dos jogadores (entre 0 e 1)."),
    VOICE_MODENABLE("voice_modenable <0/1>", "Habilita / desabilita modulação de voz."),

    VOLUME("volume <0.0-1.0>", "Define volume global do jogo (de 0 a 1).");

    // Você pode adicionar mais comandos conforme encontrar nas documentações / lista de cvars.

    private final String comando;
    private final String descricao;

    Cs2ConsoleCommand(String comando, String descricao) {
        this.comando = comando;
        this.descricao = descricao;
    }

    public String getComando() {
        return comando;
    }

    public String getDescricao() {
        return descricao;
    }

    @Override
    public String toString() {
        return comando + " — " + descricao;
    }
}

