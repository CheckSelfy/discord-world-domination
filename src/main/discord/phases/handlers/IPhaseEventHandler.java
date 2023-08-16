package discord.phases.handlers;

import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;

public interface IPhaseEventHandler {
    public void onButtonInteraction(ButtonInteractionEvent event);
}
