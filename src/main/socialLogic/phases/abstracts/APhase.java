package socialLogic.phases.abstracts;

public abstract class APhase implements IPhase{
    int round;

    public APhase(int round) {
        this.round = round;
    }

	public int getRound() { return round; }
}
