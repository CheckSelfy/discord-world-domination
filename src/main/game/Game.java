package game;

import game.Order.IAction;
import game.entities.*;

public class Game {
    private final Country[] countries;
    private int ecologyLevel = 90; // TODO: initial balance

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

    public void impactEcology(int value) { ecologyLevel += value; }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Ecology: ");
        builder.append(ecologyLevel);
        builder.append("%\n");
        for (Country country : countries) {
            builder.append(country).append("\n\n");
        }
        return builder.toString();
    }

}
