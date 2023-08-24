package game;

import java.util.ArrayList;

import game.actions.IAction;
import game.entities.Country;

public class OrderBuilder {
    private Country country = null;
    private int curBalance = 0;
    private int curMissilesCount = 0;

    private ArrayList<IAction> actions = new ArrayList<IAction>();

    public OrderBuilder addAction(IAction action) {
        if (country == null) {
            country = action.getCountry();
            curBalance = country.getBalance();
            curMissilesCount = country.getMissiles();
        } else if (country != action.getCountry()) {
            throw new RuntimeException("Actions from different countries occured in one OrderBuilder");
        }

        if (action.missileRequired()) {
            if (curMissilesCount > 0) {
                --curMissilesCount;
                actions.add(action);
            } else {
                throw new RuntimeException("Not enoght missiles");
            }
        } else {
            if (action.requreNuclear() && !country.hasNuclear()) {
                throw new RuntimeException("Nuclear required, but not developed");
            }
            if (curBalance >= action.price()) {
                curBalance -= action.price();
                actions.add(action);
            } else {
                throw new RuntimeException("Not enoght balance");
            }
        }
        return this;
    }

    public Order build() { return new Order(actions); }
}
