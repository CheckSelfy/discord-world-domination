package game.actions;

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

    public static int developNuclearEcoImpact() { return getEcoImpact("developNuclear"); }

    public static int buildMissilesPrice() { return getPrice("buildMissiles"); }

    public static int buildMissilesEcoImpact() { return getEcoImpact("buildMissiles"); }

    public static int improveEcologyPrice() { return getPrice("improveEcology"); }

    public static int improveEcologyEcoImpact() { return getEcoImpact("improveEcology"); }

    public static int shieldCityPrice() { return getPrice("shieldCity"); }

    public static int upgradeCityPrice() { return getPrice("upgradeCity"); }

    public static int upgradeCityAmount() { return getProp("upgradeCity-amount"); }

    public static int sendMissilesPrice() { return 0; }

    public static int sendMissileEcoImpact() { return getEcoImpact("sendMissile"); }

    public static int imposeSancionsPrice() { return 0; }

    public static int removeSancionsPrice() { return 0; }

    private static int getPrice(final String actionName) { return getProp(actionName + "-price"); }

    private static int getEcoImpact(final String actionName) { return getProp(actionName + "-ecologyImpact"); }

    private static int getProp(final String name) { return Integer.parseInt(properties.getProperty(name)); }

}
