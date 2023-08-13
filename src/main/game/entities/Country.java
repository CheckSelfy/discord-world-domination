package game.entities;

import java.util.Arrays;

public class Country {
    private final City[] cities;
    private final String name;

    private int balance = 1000; // TODO: initial balance;
    private boolean hasNuclear = false;

    private int missiles = 0;

    public Country(final String name, final City[] cities) {
        this.name = name;
        this.cities = cities;
    }

    @Override
    public String toString() {
        return name + " $" + balance + (hasNuclear ? " Nuc " : " ") + "\n" + Arrays.toString(cities);
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

    public void pay(int amount) { balance -= amount; }

    public void recieve(int amount) { balance += amount; }

    public boolean hasNuclear() { return hasNuclear; }

    public void developNuclear() { hasNuclear = true; }

    public int getMissiles() { return missiles; }

    public void addMissiles(int missilesAdded) { this.missiles += missilesAdded; }

    public void removeMissile() { --this.missiles; }
}
