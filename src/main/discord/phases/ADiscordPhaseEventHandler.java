package discord.phases;

import discord.DiscordIODevice;
import social_logic.Session;

public abstract class ADiscordPhaseEventHandler implements IDiscordPhaseEventHandler {
    protected final Session<DiscordIODevice, IDiscordPhaseEventHandler> session;

    public ADiscordPhaseEventHandler(Session<DiscordIODevice, IDiscordPhaseEventHandler> session) {
        this.session = session;
    }

}
