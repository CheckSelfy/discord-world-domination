package social_logic;

import social_logic.phases.handlers_interfaces.IPhaseEventHandler;

public class Session<D extends IODevice<D, H>, H extends IPhaseEventHandler> {
    private final D ioDevice;
    private H curPhase;

    public Session(D ioDevice) { this.ioDevice = ioDevice; }

    public H getPhase() { return curPhase; }

    public void setPhaseHandler(H phase) { curPhase = phase; }

    public D getIODevice() { return ioDevice; }
}
