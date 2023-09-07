package social_logic.phases.logic;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import util.Constants;

import social_logic.entities.IMember;
import social_logic.entities.Team;
import social_logic.entities.TeamBuilder;
import social_logic.entities.TeamBuilderFactory;
import social_logic.phases.handlers_interfaces.ICollectorPhaseEventHandler;

public class CollectorPhaseLogic<TB extends TeamBuilder<T>, T extends Team> {
    private final ICollectorPhaseEventHandler<TB, T> handler;

    private final List<TB> builders;
    private final TeamBuilderFactory<TB, T> factory;

    public CollectorPhaseLogic(ICollectorPhaseEventHandler<TB, T> handler, TeamBuilderFactory<TB, T> factory) {
        this.handler = handler;
        this.factory = factory;
        this.builders = new ArrayList<>();
    }

    public void collectMembers(List<Set<IMember>> members) {
        assert (members.size() == Constants.COUNTRIES_COUNT);

        for (int i = 0; i < Constants.COUNTRIES_COUNT; i++) {
            if (!members.get(i).isEmpty()) {
                TB builder = factory.create();
                builder.setMembers(members.get(i)).setDescription(Constants.teamNames.get(i));
                this.builders.add(builder);
            }
        }

        handler.nextPhase(builders);
    }
}
