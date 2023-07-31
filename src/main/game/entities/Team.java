package game.entities;

import java.util.HashSet;
import java.util.Set;

import languages.TeamLocalization;

public class Team extends TeamLocalization {
    Set<Member> members;

    public Team(Team team) {
        super((TeamLocalization) team);
        this.members = team.members;
    }

    public Team(Set<Long> usersId, TeamLocalization localization) {
        super(localization);
        assert (!usersId.isEmpty()); // creating empty Team is forbidden

        Set<Member> newMembers = new HashSet<>(usersId.size());
        for (Long user : usersId)
            newMembers.add(new Member(user, this));

        this.members = newMembers;
    }

    public Set<Member> getMembers() {
        return members;
    }

    public void setPresident(long userId) {
        members.iterator().forEachRemaining(member -> {
            if (member.getUserId() == userId)
                member.setPresident(true);
            else 
                member.setPresident(false);
        });
    }
}
