package game.actions;

import game.Game;
import game.Order.Action;
import game.entities.Country;

public class DevelopNuclearAction extends Action<Country> {
    public DevelopNuclearAction(Country country) { super(country); }

    @Override
    public void doAction(Game<Country> game) {
        game.impactEcology(ActionsProps.developNuclearEcoImpact());
        pay();
        getCountry().developNuclear();
    }

    @Override
    public int price() { return ActionsProps.developNuclearPrice(); }

}