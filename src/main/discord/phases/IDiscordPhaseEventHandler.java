package discord.phases;

import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.react.GenericMessageReactionEvent;
import social_logic.phases.handlers_interfaces.IPhaseEventHandler;

public interface IDiscordPhaseEventHandler extends IPhaseEventHandler {
    public void onButtonInteraction(ButtonInteractionEvent event);

    public void onGenericMessageReaction(GenericMessageReactionEvent event);

}
