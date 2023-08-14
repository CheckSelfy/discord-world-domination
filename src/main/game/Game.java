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

    public void transferMoney(Country from, Country to, int amount) {
        from.pay(amount);
        to.recieve(amount);
    }

    public void sendMissile(Country from, Country to, int cityIndex) { to.getCities()[cityIndex].acceptMissile(); }

    public void developNuclear(Country country) { country.developNuclear(); }

    public void addMissiles(Country country, int count) { country.addMissiles(count); }

}
