package game.entities;

import java.util.HashSet;
import java.util.Set;

import languages.TeamLocalization;

public class Team extends TeamLocalization {
    Set<Member> members;
    Member president;

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
        this.president = null;
    }

    public Set<Member> getMembers() { return members; }

    public Member getPresident() {
        return president;
    }

    public boolean isPresident(long userId) {
        return getPresident().getUserId() == userId;
    }

    public void setPresident(long userId) {
        members.iterator().forEachRemaining(member -> {
            if (member.getUserId() == userId)
                president = member;
        });
    }
}
