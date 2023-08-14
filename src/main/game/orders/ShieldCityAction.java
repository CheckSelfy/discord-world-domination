package game.orders;

import game.Game;
import game.orders.Order.Action;
import game.structs.CityPtr;;

public class ShieldCityAction extends Action {
    private int cityIndex;

    public ShieldCityAction(CityPtr city) {
        super(city.country());
        this.cityIndex = city.cityIndex();
    }

    @Override
    public void doAction(Game game) {
        game.pay(getCountry(), price());
        game.shieldCity(getCountry(), cityIndex);
    }

    @Override
    public int price() {
        return 300;
    } // TODO: prices
}
