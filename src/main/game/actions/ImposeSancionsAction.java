package game.actions;

import game.Game;
import game.Order.Action;
import game.entities.Country;

public class ImposeSancionsAction extends Action<Country> {
    private final Country to;

    public ImposeSancionsAction(final Country from, final Country to) {
        super(from);
        if (from == to) {
            throw new RuntimeException("Self sanctions");
        }
        this.to = to;
    }

    @Override
    public void doAction(Game<Country> game) { to.imposeSancions(getCountry()); }

    @Override
    public int price() { return ActionsProps.imposeSancionsPrice(); }

}
