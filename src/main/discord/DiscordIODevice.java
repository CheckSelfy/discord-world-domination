package discord;

import discord.phases.IDiscordPhaseEventHandler;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.react.GenericMessageReactionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import social_logic.IODevice;
import social_logic.Session;

public class DiscordIODevice extends ListenerAdapter implements IODevice<DiscordIODevice, IDiscordPhaseEventHandler> {
    private Session<DiscordIODevice, IDiscordPhaseEventHandler> session;
    private final long guildId;
    private final JDA jda;

    public DiscordIODevice(final JDA jda, final long guildId) {
        this.jda = jda;
        this.guildId = guildId;
    }

    public long getGuildId() {
        return guildId;
    }

    @Override
    public void setSession(Session<DiscordIODevice, IDiscordPhaseEventHandler> session) { this.session = session; }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        guard();

        if (event.getGuild().getIdLong() != guildId) {
            return;
        }
        session.getPhase().onButtonInteraction(event);
    }

    @Override
    public void onGenericMessageReaction(GenericMessageReactionEvent event) {
        guard();

        if (event.getGuild().getIdLong() != guildId) {
            return;
        }
        session.getPhase().onGenericMessageReaction(event);
    }

    @Override
    public void close() { jda.removeEventListener(this); }

    public JDA getJDA() { return jda; }

    private void guard() {
        if (session == null) {
            throw new NullPointerException("Session is not set, but event recieved");
        }
    }

}
