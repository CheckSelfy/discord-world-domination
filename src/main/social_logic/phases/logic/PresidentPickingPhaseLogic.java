package social_logic.phases.logic;

import java.util.ArrayList;
import java.util.NoSuchElementException;

import social_logic.entities.IMember;
import social_logic.entities.TeamBuilder;
import social_logic.phases.handlers_interfaces.IPresidentPickingPhaseEventHandler;

public class PresidentPickingPhaseLogic {
    private final ArrayList<TeamBuilder> builders;
    private final IPresidentPickingPhaseEventHandler handler;

    public PresidentPickingPhaseLogic(IPresidentPickingPhaseEventHandler handler, ArrayList<TeamBuilder> builders) {
        this.builders = builders;
        this.handler = handler;
    }

    public TeamBuilder getTeamBuilder(int index) { return builders.get(index); }
    public int getTeamCount() { return builders.size(); }

    public void setPresident(IMember member) {
        for (TeamBuilder builder : builders) {
            if (builder.getMembers().contains(member)) {
                builder.setPresident(member);
                return;
            }
        }

        throw new NoSuchElementException("Attempt to set president who doesn't belong to any team.");
    }
}
