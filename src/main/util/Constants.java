package util;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;

import languages.TeamLocalization;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.emoji.Emoji;

public class Constants {
    public static final ResourceBundle bundle;
    public static final Properties properties;

    public static final ArrayList<TeamLocalization> teamNames;
    public static final int COUNTRIES_COUNT = 6;

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

        teamNames = new ArrayList<>(COUNTRIES_COUNT);

        for (int i = 0; i < COUNTRIES_COUNT; i++) {
            teamNames.add(new TeamLocalization(Emoji.fromFormatted(bundle.getString("team" + i + "_emoji")),
                    bundle.getString("team" + i), Integer.parseInt(bundle.getString("team" + i + "_color"), 16)));
        }
    }

    public static EmbedBuilder getEmptyEmbedBuilder() {
        return new EmbedBuilder().setTitle(bundle.getString("game_name"));
    }
}
