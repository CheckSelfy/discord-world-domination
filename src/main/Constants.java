import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;

import net.dv8tion.jda.api.entities.emoji.Emoji;

public class Constants {
    static final ResourceBundle bundle;
    static final Properties properties;

    public final static Map<Emoji, String> EMOJIS_TO_COUNTRY;
    private final static Map<Emoji, Integer> EMOJIS_TO_COLOR;

    final static int COUNTRIES_COUNT = 6;

    static {
        properties = new Properties();
        try (FileInputStream fin = new FileInputStream("settings.properties")) {
            properties.load(fin);
        } catch (IOException e) {
            System.err.println("I/O Error with settings.properties");
            e.printStackTrace();
        }

        try {
            bundle = ResourceBundle.getBundle("languages/lang",
                    new Locale.Builder().setLanguage(properties.getProperty("locale")).build());
        } catch (Throwable e) {
            e.printStackTrace();
            throw e;
        }

        EMOJIS_TO_COUNTRY = new HashMap<>(COUNTRIES_COUNT);
        EMOJIS_TO_COLOR = new HashMap<>(COUNTRIES_COUNT);

        for (int i = 0; i < COUNTRIES_COUNT; i++) {
            EMOJIS_TO_COUNTRY.put(
                    Emoji.fromFormatted(bundle.getString("team" + i + "_emoji")),
                    bundle.getString("team" + i));

            EMOJIS_TO_COLOR.put(
                    Emoji.fromFormatted(bundle.getString("team" + i + "_emoji")),
                    Integer.parseInt(bundle.getString("team" + i + "_color"), 16));
        }
    }

    static String getFullNameOfCountry(Emoji emoji) {
        if (emoji.getFormatted().startsWith("<:")) // check for custom emoji
            return EMOJIS_TO_COUNTRY.get(emoji);
        return EMOJIS_TO_COUNTRY.get(emoji) + " " + emoji.getFormatted();
    }

    public static int getTeamColor(Emoji teamEmoji) {
        return EMOJIS_TO_COLOR.get(teamEmoji);
    }
}
