package social_logic.entities;

import java.util.HashSet;
import java.util.Set;

import languages.CountryDescription;

public class TeamBuilder {
    private Set<IMember> members;
    private IMember president;
    private CountryDescription description;

    public TeamBuilder setMembers(Set<IMember> members) {
        this.members = members;
        return this;
    }

    public TeamBuilder addMember(IMember member) {
        if (members == null) {
            members = new HashSet<>();
        }
        members.add(member);
        return this;
    }

    public TeamBuilder setPresident(IMember president) {
        this.president = president;
        return this;
    }

    public TeamBuilder setDescription(CountryDescription description) {
        this.description = description;
        return this;
    }

    public Team build() { return new Team(members, president, description); }
}
