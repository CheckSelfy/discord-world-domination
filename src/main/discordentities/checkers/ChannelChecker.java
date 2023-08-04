package discordentities.checkers;

import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;

public class ChannelChecker extends GuildChecker {
    private long channelId;

    public ChannelChecker(long guildId, long channelId) {
        super(guildId);
        this.channelId = channelId;
    }

    public boolean check(long guildId, long channelId) { return check(guildId) && channelId == this.channelId; }

    public boolean check(GuildMessageChannel ch) { return check(ch.getGuild().getIdLong(), ch.getIdLong()); }

    public long getChannelId() { return channelId; }

    public void setChannelId(long channelId) { this.channelId = channelId; }
}
