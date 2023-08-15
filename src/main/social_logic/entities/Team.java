package social_logic.entities;

import java.util.Set;

import game.entities.City;
import game.entities.Country;
import languages.CountryDescription;
import social_logic.io_entities.CombinedChannel;
import social_logic.io_entities.IUser;

public class Team extends Country {
    private Set<IUser> members;
    private IUser president;
    private CombinedChannel channel;
    private CountryDescription description;

    // TODO: STUB. Change or check it please.
    private static City[] createCities(CountryDescription localization) {
        City[] result = new City[4];
        for (int i = 0; i < result.length; i++) {
            result[i] = new City(localization.getCityNames()[i], (i + 14) * 10);
        }
        return result;
    }

    public Team(Set<IUser> members, IUser president, CountryDescription description, CombinedChannel channel) {
        super(description.getName(), createCities(description));

        assert (!members.isEmpty());
        assert (members.contains(president));

        this.members = members;
        this.president = president;
        this.channel = channel;
        this.description = description;
    }

    public boolean isMemberOfTeam(IUser user) { return members.contains(user); }

    public Set<IUser> getMembers() { return members; }

    public IUser getPresident() { return president; }

    public CombinedChannel getChannel() { return channel; }

    public CountryDescription getDescription() { return description; }
}
