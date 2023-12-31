package languages;
import net.dv8tion.jda.api.entities.emoji.Emoji;

// CountryData
public class CountryDescription {
    private Emoji emoji;
    private String name;
    private String[] cityNames;
    private int color;

    public CountryDescription(Emoji emoji, String name, String[] cityNames, int color) {
        this.emoji = emoji;
        this.name = name;
        this.cityNames = cityNames;
        this.color = color;
    }

    public Emoji getEmoji() {
        return emoji;
    }

    public String getName() {
        return name;
    }

    public int getColor() {
        return color;
    }

    public boolean isFormatted() {
        return emoji.getFormatted().startsWith("<:");
    }

    public String getFullName() {
        return name + ' ' + emoji.getName(); 
    }

    public String[] getCityNames() { return cityNames; }
}
