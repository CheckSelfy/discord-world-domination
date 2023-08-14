package game.orders;

import game.Game;
import game.entities.Country;
import game.orders.Order.Action;
import util.ActionsProps;

public class BuildMissilesAction extends Action {
    private final int count;

    public BuildMissilesAction(Country country, int count) {
        super(country);
        this.count = count;
    }

    @Override
    public void doAction(Game game) {
        game.harmEcology(ActionsProps.buildMissilesHarm() * count);
        pay();
        getCountry().addMissiles(count);
    }

    @Override
    public int price() { return ActionsProps.buildMissilesPrice() * count; }

    @Override
    public boolean requreNuclear() { return true; }
}
