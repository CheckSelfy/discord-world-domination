package game;

import java.util.List;

import game.actions.IAction;
import game.entities.*;

public class Game<C extends Country> {
    private final List<C> countries;
    private int ecologyLevel = 90; // TODO: initial balance

    public Game(List<C> countries) { this.countries = countries; }

    public List<C> getCountries() { return countries; }

    public int[] getAvgLifeLevel() {
        int[] levels = new int[countries.size()];
        for (int i = 0; i < countries.size(); i++) {
            levels[i] = countries.get(i).getLifeLevel();
        }
        return levels;
    }

    public void acceptOrder(Order order) {
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
