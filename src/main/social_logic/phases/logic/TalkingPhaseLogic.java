package social_logic.phases.logic;

import java.util.List;

import game.Game;
import social_logic.entities.Team;
import social_logic.phases.handlers_interfaces.ITalkingPhaseEventHandler;

public class TalkingPhaseLogic<T extends Team> {
    private final ITalkingPhaseEventHandler handler;
    private final Game<T> game;

    public TalkingPhaseLogic(ITalkingPhaseEventHandler handler, Game<T> game) {
        this.handler = handler;
        this.game = game;
    }

    public Game<T> getGame() { return game; }

    public List<T> getTeams() { return game.getCountries(); }

}
