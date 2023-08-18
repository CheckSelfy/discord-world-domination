package discord.phases;

import discord.DiscordIODevice;
import net.dv8tion.jda.api.JDA;
import social_logic.Session;

public abstract class ADiscordPhaseEventHandler implements IDiscordPhaseEventHandler {
    protected final Session<DiscordIODevice, IDiscordPhaseEventHandler> session;

    public ADiscordPhaseEventHandler(Session<DiscordIODevice, IDiscordPhaseEventHandler> session) {
        this.session = session;
    }

    protected JDA getJDA() { return session.getIODevice().getJDA(); }

}
