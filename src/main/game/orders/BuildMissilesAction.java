package game.orders;

import game.Game;
import game.entities.Country;
import game.orders.Order.Action;

public class BuildMissilesAction extends Action {
    private final int count;

    public BuildMissilesAction(Country country, int count) {
        super(country);
        this.count = count;
    }

    @Override
    public void doAction(Game game) { game.addMissiles(getCountry(), count); }

    @Override
    public int price() { return count * 150; }

    @Override
    public boolean requreNuclear() { return true; }

}
