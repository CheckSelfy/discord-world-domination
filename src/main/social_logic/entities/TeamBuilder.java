package social_logic.entities;

import java.util.HashSet;
import java.util.Set;

import languages.CountryDescription;

public abstract class TeamBuilder<T extends Team> {
    private Set<IMember> members;
    private IMember president;
    private CountryDescription description;

    public TeamBuilder<T> setMembers(Set<IMember> members) {
        this.members = members;
        return this;
    }

    public TeamBuilder<T> addMember(IMember member) {
        if (members == null) {
            members = new HashSet<>();
        }
        members.add(member);
        return this;
    }

    public TeamBuilder<T> setPresident(IMember president) {
        assert (members.contains(president));

        this.president = president;
        return this;
    }

    public TeamBuilder<T> setDescription(CountryDescription description) {
        this.description = description;
        return this;
    }

    public abstract T build();

    public Set<IMember> getMembers() { return members; }

    public IMember getPresident() { return president; }

    public CountryDescription getDescription() { return description; }

}
