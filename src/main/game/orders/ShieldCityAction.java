package game.orders;

import game.Game;
import game.entities.*;
import game.orders.Order.Action;;

public class ShieldCityAction extends Action {
    private int cityIndex;

    public ShieldCityAction(Country country, int cityIndex) {
        super(country);
        this.cityIndex = cityIndex;
    }

    @Override
    public void doAction(Game game) {
        game.pay(getCountry(), price());
        game.shieldCity(getCountry(), cityIndex);
    }

    @Override
    public int price() { return 300; } // TODO: prices
}
