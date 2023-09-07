package game.actions;

import game.Game;
import game.entities.Country;

public class ImproveEcologyAction extends AbAction {

    public ImproveEcologyAction(Country country) { super(country); }

    @Override
    public void doAction(Game<? extends Country> game) { game.impactEcology(ActionsProps.improveEcologyEcoImpact()); }

    @Override
    public int price() { return ActionsProps.improveEcologyPrice(); }

}
