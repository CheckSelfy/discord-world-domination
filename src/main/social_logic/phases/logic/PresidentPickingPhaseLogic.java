package social_logic.phases.logic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import social_logic.entities.IMember;
import social_logic.entities.Team;
import social_logic.entities.TeamBuilder;
import social_logic.phases.handlers_interfaces.IPresidentPickingPhaseEventHandler;

public class PresidentPickingPhaseLogic<TB extends TeamBuilder<T>, T extends Team> {
    private final List<TB> builders;
    private final IPresidentPickingPhaseEventHandler handler;
    private final List<Map<IMember, IMember>> votes;

    private final Map<IMember, Map<IMember, IMember>> memberToTeam;

    public PresidentPickingPhaseLogic(IPresidentPickingPhaseEventHandler handler,
            List<TB> builders) {
        this.builders = builders;
        this.handler = handler;
        this.votes = new ArrayList<>(builders.size());
        this.memberToTeam = new HashMap<>();
        for (int i = 0; i < builders.size(); i++) {
            Set<IMember> members = builders.get(i).getMembers();
            votes.add(new HashMap<>(members.size()));
            for (IMember member : members) {
                memberToTeam.put(member, votes.get(i));
            }
        }
    }

    public TB getTeamBuilder(int index) { return builders.get(index); }

    public int getTeamCount() { return builders.size(); }

    public void vote(IMember voter, IMember voted) { memberToTeam.get(voter).put(voter, voted); }

    public void proceedVotes() {
        for (int i = 0; i < votes.size(); i++) {
            Map<IMember, IMember> teamVotes = votes.get(i);
            Map<IMember, Integer> votesCount = new HashMap<>(teamVotes.size());
            for (IMember voted : teamVotes.values()) {
                votesCount.put(voted, votesCount.getOrDefault(voted, 0) + 1);
            }

            IMember president = null;
            int maxVotes = 0;
            for (Entry<IMember, Integer> votesForMember : votesCount.entrySet()) {
                Integer votes = votesForMember.getValue();
                if ((votes == null ? 0 : votes.intValue()) > maxVotes) {
                    president = votesForMember.getKey();
                    maxVotes = votesForMember.getValue();
                }
            }

            if (president == null) {
                president = builders.get(i).getMembers().iterator().next();
            }

            builders.get(i).setPresident(president);
        }

        handler.nextPhase();
    }

    public List<T> buildTeams() {
        List<T> teams = new ArrayList<>(builders.size());
        for (TB builder : builders) {
            teams.add(builder.build());
        }
        return teams;
    }

}
