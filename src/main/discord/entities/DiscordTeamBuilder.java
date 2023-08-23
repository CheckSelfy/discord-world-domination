package discord.entities;

import java.util.Set;

import languages.CountryDescription;
import social_logic.entities.IMember;
import social_logic.entities.TeamBuilder;

public class DiscordTeamBuilder extends TeamBuilder<DiscordTeam> {
    private DiscordTeamProperty property;

    public DiscordTeamProperty getProperty() { return property; }

    public void setProperty(DiscordTeamProperty property) { this.property = property; }

    @Override
    public DiscordTeam build() { return new DiscordTeam(getMembers(), getPresident(), getDescription(), property); }

    @Override
    public DiscordTeamBuilder addMember(IMember member) {
        super.addMember(member);
        return this;
    }

    @Override
    public DiscordTeamBuilder setDescription(CountryDescription description) {
        super.setDescription(description);
        return this;
    }

    @Override
    public DiscordTeamBuilder setMembers(Set<IMember> members) {
        super.setMembers(members);
        return this;
    }

    @Override
    public DiscordTeamBuilder setPresident(IMember president) {
        super.setPresident(president);
        return this;
    }

}
