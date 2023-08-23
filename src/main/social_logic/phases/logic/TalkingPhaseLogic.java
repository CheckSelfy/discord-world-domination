package social_logic.phases.logic;

import java.util.List;

import social_logic.entities.Team;

public class TalkingPhaseLogic<T extends Team> {
    private final List<T> teams;

    public TalkingPhaseLogic(List<T> teams) { this.teams = teams; }

}
