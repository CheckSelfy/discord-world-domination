package discord;

import discord.phases.handlers.CollectorPhaseHandler;
import discord.phases.handlers.IPhaseEventHandler;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import social_logic.SocialLogicManager;

public class Session extends ListenerAdapter {
    private JDA jda;
    private DiscordGuild guild;
    private final SocialLogicManager slm;
    private IPhaseEventHandler curPhase;
    // I assume that we can differ games only by guild => different games have
    // different guilds. But it can be easily changed to DiscordCategory.

    public Session(JDA jda, DiscordGuild guild) {
        this.jda = jda;
        this.guild = guild;
        slm = new SocialLogicManager();
        curPhase = new CollectorPhaseHandler(this);
    }

    public JDA getJDA() { return jda; }

    public DiscordGuild getDiscordGuild() { return guild; }

    public long getGuildId() { return guild.getId(); }

    public net.dv8tion.jda.api.entities.Guild getGuild() { return jda.getGuildById(guild.getId()); }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        if (event.getGuild().getIdLong() != guild.getId()) {
            return;
        }
        curPhase.onButtonInteraction(event);
    }

    public void setPhase(IPhaseEventHandler phase) { curPhase = phase; }

    public SocialLogicManager getSocialLogicManager() { return slm; }
}
