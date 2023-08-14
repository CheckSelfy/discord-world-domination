package discordentities;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import socialLogic.IOEntities.ITextChannel;
import socialLogic.IOEntities.IUser;
import socialLogic.IOEntities.IVoiceChannel;

public class DiscordVoiceChannel implements ITextChannel, IVoiceChannel {
    private DiscordManager manager;
    private long id;

    @Override
    public void moveUser(IUser user) {
        Guild guild = manager.getGuild();
        guild.moveVoiceMember(guild.getMemberById(((DiscordUser) user).getUserId()), getChannel()).queue();
        // It's not bad to explicit cast, because it's guarantee that social logic have only one IUser type.
    }

    public VoiceChannel getChannel() { return manager.getJDA().getVoiceChannelById(getId()); }

    @Override
    public void sendPlainMessage(String content) { getChannel().sendMessage(content).queue(); }

    public long getId() { return id; }

}
