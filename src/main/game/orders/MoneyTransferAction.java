package game.orders;

import game.Game;
import game.entities.Country;
import game.orders.Order.Action;

public class MoneyTransferAction extends Action {
    private final Country transferTo;
    private final int amount;

    public MoneyTransferAction(Country from, Country to, int amount) {
        super(from);
        this.transferTo = to;
        this.amount = amount;
    }

    @Override
    public void doAction(Game game) { game.transferMoney(getCountry(), transferTo, amount); }

    @Override
    public int price() { return amount; }

}
