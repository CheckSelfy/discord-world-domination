package game.orders;

import game.Game;
import game.entities.Country;
import game.orders.Order.Action;
import game.structs.CityPtr;

public class SendMissileAction extends Action {
    private final CityPtr city;

    public SendMissileAction(Country country, CityPtr city) {
        super(country);
        this.city = city;
    }

    @Override
    public void doAction(Game game) {
        getCountry().removeMissile();
        game.sendMissile(getCountry(), city.country(), city.cityIndex());
    }

    @Override
    public int price() { return 0; }

    @Override
    public boolean missileRequired() { return true; }

}
