package game.orders;

import game.Game;
import game.entities.Country;
import game.orders.Order.Action;

public class ShieldCityAction extends Action {
    private final int cityIndex;

    public ShieldCityAction(Country country, int cityIndex) {
        super(country);
        this.cityIndex = cityIndex;
    }

    @Override
    public void doAction(Game game) {
        pay();
        getCountry().getCities()[cityIndex].setShield();
    }

    @Override
    public int price() { return 300; } // TODO: prices
}
