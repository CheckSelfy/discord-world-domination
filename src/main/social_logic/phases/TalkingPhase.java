package social_logic.phases;

import social_logic.phases.abstracts.APhase;
import social_logic.phases.abstracts.IPhase;

public class TalkingPhase extends APhase {
    public TalkingPhase(int round) { super(round); }

    @Override
    public IPhase nextPhase() { return new OrdersPhase(getRound()); }

}
