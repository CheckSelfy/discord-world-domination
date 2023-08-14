package game.orders;

import game.Game;
import game.entities.Country;
import game.orders.Order.Action;

public class ImproveEcologyAction extends Action {

    public ImproveEcologyAction(Country country) { super(country); }

    @Override
    public void doAction(Game game) { game.improveEcology(); }

    @Override
    public int price() { return 200; }

}
