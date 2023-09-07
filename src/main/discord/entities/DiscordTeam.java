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

    public DiscordTeamProperty getProperty() { return property; }

    @Override
    public int hashCode() { return (property == null) ? 0 : property.hashCode(); }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (obj instanceof DiscordTeam other) {
            if (property == null) {
                if (other.property == null) {
                    return true;
                }
            } else if (property.equals(other.property)) {
                return true;
            }
        }
        return false;
    }

}
