package social_logic.phases.logic;

import java.util.ArrayList;
import java.util.Set;

import util.Constants;

import social_logic.entities.IMember;
import social_logic.entities.TeamBuilder;
import social_logic.phases.handlers_interfaces.ICollectorPhaseEventHandler;

public class CollectorPhaseLogic {
    private final ICollectorPhaseEventHandler handler;

    private final ArrayList<TeamBuilder> builders;

    public CollectorPhaseLogic(ICollectorPhaseEventHandler handler) {
        this.handler = handler;
        this.builders = new ArrayList<>(Constants.COUNTRIES_COUNT);
        for (int i = 0; i < Constants.COUNTRIES_COUNT; i++) {
            builders.add(new TeamBuilder());
        }
    }

    public void collectMembers(ArrayList<Set<IMember>> members) {
        for (int i = 0; i < Constants.COUNTRIES_COUNT; i++) {
            builders.get(i)
                    .setMembers(members.get(i))
                    .setDescription(Constants.teamNames.get(i));
        }
        handler.nextPhase(builders);

    }
}
