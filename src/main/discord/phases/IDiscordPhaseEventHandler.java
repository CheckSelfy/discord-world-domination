package discord.phases;

import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.events.message.react.GenericMessageReactionEvent;
import social_logic.phases.handlers_interfaces.IPhaseEventHandler;

public interface IDiscordPhaseEventHandler extends IPhaseEventHandler {
    static final String DEBUG_DEFAULT_REPLY = "[DEBUG] Default reply";

    default void onButtonInteraction(ButtonInteractionEvent event) { event.reply(DEBUG_DEFAULT_REPLY); }

    default void onGenericMessageReaction(GenericMessageReactionEvent event) {};

    default void onStringSelectInteraction(StringSelectInteractionEvent event) { event.reply(DEBUG_DEFAULT_REPLY); };

}
