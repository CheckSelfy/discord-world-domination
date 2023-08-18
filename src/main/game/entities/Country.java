package game.entities;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Country {
    private final String name;
    private final City[] cities;

    private int balance = 1000; // TODO: initial balance

    private boolean hasNuclear = false;
    private int missiles = 0;

    private Set<Country> sanctions = null;

    public Country(final String name, final City[] cities) {
        this.name = name;
        this.cities = cities;
    }

    public int getLifeLevel() {
        int sum = 0;
        for (City city : cities) {
            sum += city.getLifeLevel();
        }
        return sum / cities.length;
    }

    public City[] getCities() { return cities; }

    public int getBalance() { return balance; }

    // Money
    public void pay(int amount) { balance -= amount; }

    public void recieve(int amount) { balance += amount; }

    // Nuclear
    public boolean hasNuclear() { return hasNuclear; }

    public void developNuclear() { hasNuclear = true; }

    // Missiles
    public int getMissiles() { return missiles; }

    public void addMissiles(int missilesAdded) { this.missiles += missilesAdded; }

    public void removeMissile() { --this.missiles; }

    // Sanctions
    public void imposeSancions(final Country from) {
        if (sanctions == null) {
            sanctions = new HashSet<>();
        }
        sanctions.add(from);
    }

    public void removeSanctions(final Country from) { sanctions.remove(from); }

    public int sanctionsCount() { return sanctions == null ? 0 : sanctions.size(); }

    public boolean isSanctionsImposed(final Country from) {
        return sanctions == null ? false : sanctions.contains(from);
    }

    public Set<Country> sanctionsSet() { return sanctions == null ? Set.of() : sanctions; }

    // Object
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(name);
        sb.append(" $");
        sb.append(balance);
        sb.append(" ");
        if (hasNuclear()) {
            sb.append("Nuc ");
            sb.append(missiles);
            sb.append(" ");
        }
        if (sanctionsCount() > 0) {
            sb.append('{');
            sb.append(String.join(" ", sanctions.stream().map(country -> country.name).toList()));
            sb.append('}');
        }
        sb.append("\n");
        sb.append(Arrays.toString(cities));
        return sb.toString();
    }

}
