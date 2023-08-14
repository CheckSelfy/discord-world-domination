package util;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ActionsProps {
    private static final String pricesPath = "prices.properties";
    private static final Properties properties;

    static {
        properties = new Properties();
        try (FileInputStream in = new FileInputStream(pricesPath)) {
            properties.load(in);
        } catch (IOException e) {
            System.err.println("I/O Error with " + pricesPath);
        }
    }

    public static int developNuclearPrice() { return getPrice("developNuclear"); }

    public static int developNuclearHarm() { return getHarm("developNuclear"); }

    public static int buildMissilesPrice() { return getPrice("buildMissiles"); }

    public static int buildMissilesHarm() { return getHarm("buildMissiles"); }

    public static int improveEcologyPrice() { return getPrice("improveEcology"); }

    public static int shieldCityPrice() { return getPrice("shieldCity"); }

    public static int upgradeCityPrice() { return getPrice("upgradeCity"); }

    private static int getPrice(final String actionName) {
        return Integer.parseInt(properties.getProperty(actionName + "-price"));
    }

    private static int getHarm(final String actionName) {
        return Integer.parseInt(properties.getProperty(actionName + "-ecologyHarm"));
    }
}
