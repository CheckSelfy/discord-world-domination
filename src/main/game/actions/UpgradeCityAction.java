package game.actions;

import game.Game;
import game.entities.Country;

public class UpgradeCityAction extends AbAction {
    private final int cityIndex;

    public UpgradeCityAction(Country country, int cityIndex) {
        super(country);
        this.cityIndex = cityIndex;
    }

    @Override
    public void doAction(Game<? extends Country> game) {
        pay();
        getCountry().getCities()[cityIndex].upgrade(ActionsProps.upgradeCityAmount());
    }

    @Override
    public int price() { return ActionsProps.upgradeCityPrice(); }

}
