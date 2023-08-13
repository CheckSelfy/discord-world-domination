package game;

import game.orders.*;
import game.orders.Order.IAction;
import game.entities.*;

public class Game {
    private final Country[] countries;
    // private int ecologyLevel = 90;

    public Game(Country[] countries) { this.countries = countries; }

    public Country[] getCountries() { return countries; }

    public int[] getAvgLifeLevel() {
        int[] levels = new int[countries.length];
        for (int i = 0; i < countries.length; i++) {
            levels[i] = countries[i].getLifeLevel();
        }
        return levels;
    }

    public void acceptOrder(final Order order) {
        for (IAction action : order.getActions()) {
            action.doAction(this);
        }
    }

    // Methods

    public void shieldCity(Country country, int cityIndex) { country.getCities()[cityIndex].setShield(); }

    public void pay(Country country, int price) { country.pay(price); }
}