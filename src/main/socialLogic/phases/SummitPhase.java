package socialLogic.phases;

import socialLogic.phases.abstracts.APhase;
import socialLogic.phases.abstracts.IPhase;

public class SummitPhase extends APhase {

    public SummitPhase(int round) { super(round); }

    @Override
    public IPhase nextPhase() { return new TalkingPhase(getRound() + 1); }

}
