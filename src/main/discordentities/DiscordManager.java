package discordentities;

import net.dv8tion.jda.api.JDA;

public class DiscordManager {
    private JDA jda;
    private DiscordGuild guild;
    // I assume that we can differ games only by guild => different games have
    // different guilds. But it can be easily changed to category.

    public DiscordManager(JDA jda, DiscordGuild guild) {
        this.jda = jda;
        this.guild = guild;
    }

    public JDA getJDA() { return jda; }

    // Bad naming though
    public DiscordGuild getDiscordGuild() { return guild; }

    public net.dv8tion.jda.api.entities.Guild getGuild() { return jda.getGuildById(guild.getId()); }
}
