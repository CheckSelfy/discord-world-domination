package game.actions;

import game.Game;
import game.Order.Action;
import game.entities.City;
import game.entities.Country;

public class SendMissileAction extends Action {
    private final City city;

    public SendMissileAction(Country country, City city) {
        super(country);
        this.city = city;
    }

    @Override
    public void doAction(Game game) {
        game.harmEcology(5);
        getCountry().removeMissile();
        city.acceptMissile();
    }

    @Override
    public int price() { return 0; }

    @Override
    public boolean missileRequired() { return true; }

}
