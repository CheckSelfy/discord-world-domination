package game.orders;

import java.util.ArrayList;

import game.entities.Country;
import game.orders.Order.IAction;

public class OrderBuilder {
    private Country country = null;
    private int curBalance = 0;
    // private int curBombsCount;

    private ArrayList<IAction> actions = new ArrayList<IAction>();

    public OrderBuilder addAction(IAction action) {
        if (country == null) {
            country = action.getCountry();
            curBalance = country.getBalance();
        } else if (country != action.getCountry()) {
            throw new RuntimeException("Actions from different countries occured in one OrderBuilder");
        }

        if (curBalance >= action.price()) {
            curBalance -= action.price();
            actions.add(action);
        }
        return this;
    }

    public Order build() { return new Order(actions.toArray(new IAction[0])); }
}
