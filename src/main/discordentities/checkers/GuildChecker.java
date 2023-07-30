package discordentities.checkers;

import net.dv8tion.jda.api.entities.Guild;

public class GuildChecker {
    private long guildId;

    public GuildChecker(long guildId) {
        this.guildId = guildId;
    }

    public boolean check(long guildId) {
        return guildId == this.guildId;
    }

    public boolean check(Guild guild) {
        return check(guild.getIdLong());
    }

    public long getGuildId() {
        return guildId;
    }

    public void setGuildId(long guildId) {
        this.guildId = guildId;
    }
}
