package socialLogic.entities;

import java.util.Set;

import game.entities.Country;
import languages.TeamLocalization;
import socialLogic.IOEntities.ACombinedChannel;
import socialLogic.IOEntities.IUser;

public class Team extends Country {
    Set<IUser> members;
    IUser president;

    TeamLocalization localization;
    ACombinedChannel channel;

    public Team(Set<IUser> members, IUser president, TeamLocalization localization, ACombinedChannel channel) {
        assert (!members.isEmpty());
        assert (members.contains(president));

        this.members = members;
        this.president = president;
        this.localization = localization;
        this.channel = channel;
    }

    public TeamLocalization getLocalization() { return localization; }

    public boolean isMemberOfTeam(IUser user) { return members.contains(user); }

}
