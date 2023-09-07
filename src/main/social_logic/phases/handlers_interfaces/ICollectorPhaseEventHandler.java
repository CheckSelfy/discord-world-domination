package social_logic.phases.handlers_interfaces;

import java.util.List;

import social_logic.entities.Team;
import social_logic.entities.TeamBuilder;

public interface ICollectorPhaseEventHandler<TB extends TeamBuilder<T>, T extends Team> extends IPhaseEventHandler {
    public void nextPhase(List<TB> builders);
}
