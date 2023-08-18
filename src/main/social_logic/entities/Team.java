package social_logic.entities;

import java.util.Set;

import game.entities.City;
import game.entities.Country;
import languages.CountryDescription;

public class Team extends Country {
    private final Set<IMember> members;
    private final IMember president;
    private final CountryDescription description;

    public Team(Set<IMember> members, IMember president, CountryDescription description) {
        super(description.getName(), CreateCities.createCities(description)); // ?

        assert (!members.isEmpty());
        assert (members.contains(president));

        this.members = members;
        this.president = president;
        this.description = description;
    }

    // TODO: remove if unused
    public boolean isMemberOfTeam(IMember user) { return members.contains(user); }

    public Set<IMember> getMembers() { return members; }

    public IMember getPresident() { return president; }

    public CountryDescription getDescription() { return description; }
}

// TODO: STUB. Change or check it please.
class CreateCities {
    public static City[] createCities(CountryDescription localization) {
        City[] result = new City[4];
        for (int i = 0; i < result.length; i++) {
            result[i] = new City(localization.getCityNames()[i], (i + 14) * 10);
        }
        return result;
    }
}
