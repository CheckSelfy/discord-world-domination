package socialLogic.phases;

import socialLogic.phases.abstracts.APhase;
import socialLogic.phases.abstracts.IPhase;

public class OrdersPhase extends APhase {

    public OrdersPhase(int round) { super(round); }

    @Override
    public IPhase nextPhase() { return new SummitPhase(getRound()); }

}
