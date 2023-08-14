package discordentities;

import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import socialLogic.IOEntities.ITextChannel;

public class DiscordTextChannel implements ITextChannel {
    private DiscordManager manager;
    private long id;

    @Override
    public void sendPlainMessage(String content) { getChannel().sendMessage(content).queue(); }

    public TextChannel getChannel() { return manager.getJDA().getTextChannelById(getId()); }

    public long getId() { return id; }
}
