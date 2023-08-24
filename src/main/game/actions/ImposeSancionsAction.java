package game.actions;

import game.Game;
import game.entities.Country;

public class ImposeSancionsAction extends AbAction {
    private final Country to;

    public ImposeSancionsAction(final Country from, final Country to) {
        super(from);
        if (from == to) {
            throw new RuntimeException("Self sanctions");
        }
        this.to = to;
    }

    @Override
    public void doAction(Game<? extends Country> game) { to.imposeSancions(getCountry()); }

    @Override
    public int price() { return ActionsProps.imposeSancionsPrice(); }

}
