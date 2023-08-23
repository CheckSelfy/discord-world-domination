package discord.entities;

import java.util.Set;

import languages.CountryDescription;
import social_logic.entities.IMember;
import social_logic.entities.Team;

public class DiscordTeam extends Team {
    private final DiscordTeamProperty property;

    public DiscordTeam(Set<IMember> members, IMember president, CountryDescription description,
            DiscordTeamProperty property) {
        super(members, president, description);
        this.property = property;
    }

}
