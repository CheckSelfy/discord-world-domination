package game.entities;

import java.util.Arrays;

public class Country {
    private final City[] cities;
    private final String name;

    private int balance = 1000; // TODO: initial balance;

    public Country(final String name, final City[] cities) {
        this.name = name;
        this.cities = cities;
    }

    @Override
    public String toString() { return name + " $" + balance + " " + Arrays.toString(cities); }

    public int getLifeLevel() {
        int sum = 0;
        for (City city : cities) {
            sum += city.getLifeLevel();
        }
        return sum / cities.length;
    }

    public City[] getCities() { return cities; }

    public int getBalance() { return balance; }

    public void pay(int price) { balance -= price; }

    public void recieve(int amount) { balance += amount; }
}
