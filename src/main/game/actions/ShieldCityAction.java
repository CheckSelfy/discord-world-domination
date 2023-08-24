package game.actions;

import game.Game;
import game.entities.Country;

public class ShieldCityAction extends AbAction {
    private final int cityIndex;

    public ShieldCityAction(Country country, int cityIndex) {
        super(country);
        this.cityIndex = cityIndex;
    }

    @Override
    public void doAction(Game<? extends Country> game) {
        pay();
        getCountry().getCities()[cityIndex].setShield();
    }

    @Override
    public int price() { return ActionsProps.shieldCityPrice(); }
}
