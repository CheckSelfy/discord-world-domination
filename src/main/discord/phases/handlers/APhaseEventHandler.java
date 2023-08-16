package discord.phases.handlers;

import discord.Session;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;

public abstract class APhaseEventHandler implements IPhaseEventHandler {
    protected final Session session;

    public APhaseEventHandler(Session session) { this.session = session; }

    public abstract void onButtonInteraction(ButtonInteractionEvent event);

}
