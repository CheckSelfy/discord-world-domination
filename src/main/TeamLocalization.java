import net.dv8tion.jda.api.entities.emoji.Emoji;

public class TeamLocalization {
    private Emoji emoji;
    private String name;
    private int color;

    public TeamLocalization(Emoji emoji, String name, int color) {
        this.emoji = emoji;
        this.name = name;
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

    public String getFullName() {
        if (emoji.getFormatted().startsWith("<:"))  { // check for custom emoji (discord can't show them in most of places)
            return name;
        } else {
            return name + ' ' + emoji.getName(); // otherwise, add emoji
        }
    }
}
