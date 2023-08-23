package discord.phases;

import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.events.message.react.GenericMessageReactionEvent;
import social_logic.phases.handlers_interfaces.IPhaseEventHandler;

public interface IDiscordPhaseEventHandler extends IPhaseEventHandler {
    public void phaseEnding();

    public int getDurationInMilliseconds();

    static final String DEBUG_DEFAULT_REPLY = "[DEBUG] Default reply";

    public default void onButtonInteraction(ButtonInteractionEvent event) { event.reply(DEBUG_DEFAULT_REPLY).queue(); }

    public default void onGenericMessageReaction(GenericMessageReactionEvent event) {};

    public default void onStringSelectInteraction(StringSelectInteractionEvent event) {
        event.reply(DEBUG_DEFAULT_REPLY);
    };
}
