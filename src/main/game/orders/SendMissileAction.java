package game.orders;

import game.Game;
import game.entities.Country;
import game.orders.Order.Action;

public class SendMissileAction extends Action {
    private final Country to;
    private final int cityIndex;

    public SendMissileAction(Country country, Country to, int cityIndex) {
        super(country);
        this.to = to;
        this.cityIndex = cityIndex;
    }

    @Override
    public void doAction(Game game) {
        getCountry().removeMissile();
        game.sendMissile(getCountry(), to, cityIndex);
    }

    @Override
    public int price() { return 0; }

    @Override
    public boolean missileRequired() { return true; }

}
