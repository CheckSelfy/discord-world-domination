package game.actions;

import game.Game;
import game.Order.Action;
import game.entities.Country;

public class BuildMissilesAction extends Action {
    private final int count;

    public BuildMissilesAction(Country country, int count) {
        super(country);
        this.count = count;
    }

    @Override
    public void doAction(Game game) {
        game.impactEcology(ActionsProps.buildMissilesEcoImpact() * count);
        pay();
        getCountry().addMissiles(count);
    }

    @Override
    public int price() { return ActionsProps.buildMissilesPrice() * count; }

    @Override
    public boolean requreNuclear() { return true; }
}
