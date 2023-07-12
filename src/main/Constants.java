import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;

public class Constants {
    static final ResourceBundle bundle;
    static final Properties properties;

    final static String[] COUNTRIES = {"ru", "us", "de", "cn", "ir", "kp"};
    final static String[] FULL_COUNTRIES_NAME;
    final static String[] EMOJIS_COUNTRY;

    final static Map<String, String> COUNTRY_TO_EMOJI;
    final static Map<String, String> EMOJI_TO_COUNTRY;

    final static int COUNTRIES_COUNT = COUNTRIES.length;

    static {
        properties = new Properties();
        try (FileInputStream fin = new FileInputStream("settings.properties")) {
            properties.load(fin);
        } catch (IOException e) {
            System.err.println("I/O Error with settings.properties");
            e.printStackTrace();
        }

        try {
            bundle = ResourceBundle.getBundle("languages/lang", new Locale.Builder().setLanguage(properties.getProperty("locale")).build());
        } catch (Throwable e) {
            e.printStackTrace();
            throw e;
        }

        COUNTRY_TO_EMOJI = new HashMap<>(COUNTRIES_COUNT);
        EMOJI_TO_COUNTRY = new HashMap<>(COUNTRIES_COUNT);
        EMOJIS_COUNTRY = new String[COUNTRIES_COUNT];
        FULL_COUNTRIES_NAME = new String[COUNTRIES_COUNT];
        int lastAdded = -1;
        for (String country: COUNTRIES) {
            String unicodeEmoji = bundle.getString(country + "_emoji");
            COUNTRY_TO_EMOJI.put(country, unicodeEmoji);
            EMOJI_TO_COUNTRY.put(unicodeEmoji, country);
            EMOJIS_COUNTRY[++lastAdded] = unicodeEmoji;
            FULL_COUNTRIES_NAME[lastAdded] = bundle.getString(country) + " " + unicodeEmoji;
        }
    }
}
