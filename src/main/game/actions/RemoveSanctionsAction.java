package game.actions;

import game.Game;
import game.Order.Action;
import game.entities.Country;

public class RemoveSanctionsAction extends Action<Country> {
    private final Country to;

    public RemoveSanctionsAction(final Country from, final Country to) {
        super(from);
        if (!to.isSanctionsImposed(from)) {
            throw new RuntimeException("Try to remove sanctions, but not imposed");
        }
        this.to = to;
    }

    @Override
    public void doAction(Game<Country> game) { to.removeSanctions(getCountry()); }

    @Override
    public int price() { return ActionsProps.removeSancionsPrice(); }

}
