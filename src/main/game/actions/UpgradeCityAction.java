package game.actions;

import game.Game;
import game.Order.Action;
import game.entities.Country;

public class UpgradeCityAction extends Action<Country> {
    private final int cityIndex;

    public UpgradeCityAction(Country country, int cityIndex) {
        super(country);
        this.cityIndex = cityIndex;
    }

    @Override
    public void doAction(Game<Country> game) {
        pay();
        getCountry().getCities()[cityIndex].upgrade(ActionsProps.upgradeCityAmount());
    }

    @Override
    public int price() { return ActionsProps.upgradeCityPrice(); }

}
