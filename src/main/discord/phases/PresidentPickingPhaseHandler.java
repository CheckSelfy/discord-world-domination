package discord.phases;

import java.util.ArrayList;

import discord.DiscordIODevice;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.react.GenericMessageReactionEvent;
import social_logic.Session;
import social_logic.entities.TeamBuilder;
import social_logic.phases.handlers_interfaces.IPresidentPickingPhaseEventHandler;

public class PresidentPickingPhaseHandler extends ADiscordPhaseEventHandler
        implements IPresidentPickingPhaseEventHandler {
    private final ArrayList<TeamBuilder> builders;

    public PresidentPickingPhaseHandler(Session<DiscordIODevice, IDiscordPhaseEventHandler> session,
            ArrayList<TeamBuilder> builders) {
        super(session);
        this.builders = builders;
    }

    public void nextPhase() { /* session.setPhase(new TalkingPhase(session)); */ }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) { // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'onButtonInteraction'");
    }

    @Override
    public void onGenericMessageReaction(GenericMessageReactionEvent event) { // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'onGenericMessageReaction'");
    }
}
