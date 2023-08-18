package social_logic.phases.handlers_interfaces;

import java.util.ArrayList;

import social_logic.entities.TeamBuilder;

public interface ICollectorPhaseEventHandler extends IPhaseEventHandler {
    public void nextPhase(ArrayList<TeamBuilder> builders);
}
