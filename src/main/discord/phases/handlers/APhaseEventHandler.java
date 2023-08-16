package discord.phases.handlers;

import discord.Session;
import discord.phases.IPhaseLogic;

public abstract class APhaseEventHandler implements IPhaseEventHandler {
    protected final Session session;

    protected final IPhaseLogic logic;

    public APhaseEventHandler(Session session, IPhaseLogic logic) {
        this.session = session;
        this.logic = logic;
    }

}
