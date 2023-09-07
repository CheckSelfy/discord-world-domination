package game.actions;

import game.Game;
import game.entities.Country;

public class DevelopNuclearAction extends AbAction {
    public DevelopNuclearAction(Country country) { super(country); }

    @Override
    public void doAction(Game<? extends Country> game) {
        game.impactEcology(ActionsProps.developNuclearEcoImpact());
        pay();
        getCountry().developNuclear();
    }

    @Override
    public int price() { return ActionsProps.developNuclearPrice(); }

}