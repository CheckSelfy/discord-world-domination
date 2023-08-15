package game.actions;

import game.Game;
import game.Order.Action;
import game.entities.Country;

public class ImproveEcologyAction extends Action {

    public ImproveEcologyAction(Country country) { super(country); }

    @Override
    public void doAction(Game game) { game.impactEcology(ActionsProps.improveEcologyEcoImpact()); }

    @Override
    public int price() { return ActionsProps.improveEcologyPrice(); }

}