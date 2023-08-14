package game.orders;

import game.Game;
import game.entities.Country;
import game.orders.Order.Action;

public class DevelopNuclearAction extends Action {
    public DevelopNuclearAction(Country country) { super(country); }

    @Override
    public void doAction(Game game) {
        getCountry().pay(price());
        getCountry().developNuclear();
    }

    @Override
    public int price() { return 500; }

}