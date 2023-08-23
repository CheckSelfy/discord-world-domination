package social_logic.phases.handlers_interfaces;

import java.util.List;

import social_logic.entities.TeamBuilder;

public interface ICollectorPhaseEventHandler<TB extends TeamBuilder> extends IPhaseEventHandler {
    public void nextPhase(List<TB> builders);
}
