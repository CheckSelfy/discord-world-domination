package discord.phases.handlers;

import discord.Session;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;

public class CollectorPhaseHandler extends APhaseEventHandler {
    public CollectorPhaseHandler(Session session) { super(session); }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        //
        //
    }

    /* some method */ {
        // decided to change to next phase
        nextPhase();
    }

    public void nextPhase() { session.setPhase(new PresidentPickingPhaseHandler(session)); }
}
