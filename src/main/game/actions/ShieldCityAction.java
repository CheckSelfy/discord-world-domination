package game.actions;

import game.Game;
import game.Order.Action;
import game.entities.Country;

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
    public int price() { return ActionsProps.shieldCityPrice(); }
}
