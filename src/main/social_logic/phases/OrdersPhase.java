package social_logic.phases;

import social_logic.phases.abstracts.APhase;
import social_logic.phases.abstracts.IPhase;

public class OrdersPhase extends APhase {

    public OrdersPhase(int round) { super(round); }

    @Override
    public IPhase nextPhase() { return new SummitPhase(getRound()); }

}
