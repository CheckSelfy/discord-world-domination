import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;

public class Constants {
    static final ResourceBundle bundle;
    static final Properties properties;

    final static Set<String> COUNTRIES = Set.of(
        "ru", "us", "de", "cn", "ir", "kp"
    );

    final static Map<String, String> COUNTRY_TO_EMOJI;
    final static Map<String, String> EMOJI_TO_COUNTRY;

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

        COUNTRY_TO_EMOJI = new HashMap<>(COUNTRIES.size());
        EMOJI_TO_COUNTRY = new HashMap<>(COUNTRIES.size());
        for (String country: COUNTRIES) {
            String unicodeEmoji = bundle.getString(country + "_emoji");
            COUNTRY_TO_EMOJI.put(country, unicodeEmoji);
            EMOJI_TO_COUNTRY.put(unicodeEmoji, country);
        }

        EMOJI_TO_COUNTRY.keySet().stream().forEach(emoji -> System.out.println(emoji));
        EMOJI_TO_COUNTRY.values().stream().forEach(emoji -> System.out.println(emoji));
    }
}
