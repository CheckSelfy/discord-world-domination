package discordentities;

import java.util.Set;

import game.entities.Team;
import languages.TeamLocalization;

public class DiscordTeam extends Team {
    // which messages do i need to store?
    //      1) pickPresident
    //      2) doActions
    //      3) sendDelegation
    //      4) receiveDelegation
    //      5) joinDelegation

    public DiscordTeam(Team team) {
        super(team);
    }

    public DiscordTeam(Set<Long> usersId, TeamLocalization localization) {
        super(usersId, localization);
    }

    
}
