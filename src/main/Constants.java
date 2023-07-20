import java.io.FileInputStream;
import java.io.IOException;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;

import net.dv8tion.jda.api.entities.emoji.Emoji;

public class Constants {
    static final ResourceBundle bundle;
    static final Properties properties;

    public final static TeamLocalization[] teamNames;

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

        teamNames = new TeamLocalization[COUNTRIES_COUNT];

        for (int i = 0; i < COUNTRIES_COUNT; i++) {
            teamNames[i] = new TeamLocalization(
                    Emoji.fromFormatted(bundle.getString("team" + i + "_emoji")),
                    bundle.getString("team" + i),
                    Integer.parseInt(bundle.getString("team" + i + "_color"), 16));
        }
    }
}
