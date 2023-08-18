package social_logic;

import java.io.Closeable;

import social_logic.phases.handlers_interfaces.IPhaseEventHandler;

public interface IODevice<D extends IODevice<D, H>, H extends IPhaseEventHandler> extends Closeable {
    public void setSession(Session<D, H> session);
}
