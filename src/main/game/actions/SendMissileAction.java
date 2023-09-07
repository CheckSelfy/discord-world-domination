package game.actions;

import game.Game;
import game.entities.City;
import game.entities.Country;

public class SendMissileAction extends AbAction {
    private final City city;

    public SendMissileAction(Country country, City city) {
        super(country);
        this.city = city;
    }

    @Override
    public void doAction(Game<? extends Country> game) {
        game.impactEcology(ActionsProps.sendMissileEcoImpact());
        getCountry().removeMissile();
        city.acceptMissile();
    }

    @Override
    public int price() { return ActionsProps.sendMissilesPrice(); }

    @Override
    public boolean missileRequired() { return true; }

}
