package social_logic.phases;

import social_logic.phases.abstracts.APhase;
import social_logic.phases.abstracts.IPhase;

public class SummitPhase extends APhase {

    public SummitPhase(int round) { super(round); }

    @Override
    public IPhase nextPhase() { return new TalkingPhase(getRound() + 1); }

}
