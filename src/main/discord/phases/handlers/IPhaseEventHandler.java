package discord.phases.handlers;

import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.react.GenericMessageReactionEvent;

public interface IPhaseEventHandler {
    public void onButtonInteraction(ButtonInteractionEvent event);

    public void onGenericMessageReaction(GenericMessageReactionEvent event);

}
