package socialLogic.phases;

import socialLogic.phases.abstracts.APhase;
import socialLogic.phases.abstracts.IPhase;

public class TalkingPhase extends APhase {
    public TalkingPhase(int round) { super(round); }

    @Override
    public IPhase nextPhase() { return new OrdersPhase(getRound()); }

}
