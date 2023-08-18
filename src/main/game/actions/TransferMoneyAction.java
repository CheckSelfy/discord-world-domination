package game.actions;

import game.Game;
import game.Order.Action;
import game.entities.Country;

public class TransferMoneyAction extends Action {
    private final Country transferTo;
    private final int amount;

    public TransferMoneyAction(Country from, Country to, int amount) {
        super(from);
        this.transferTo = to;
        this.amount = amount;
    }

    @Override
    public void doAction(Game game) {
        getCountry().pay(amount);
        transferTo.recieve(amount);
    }

    @Override
    public int price() { return amount; }

}
