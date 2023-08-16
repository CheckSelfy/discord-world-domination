package discord;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import social_logic.io_entities.ITextChannel;
import social_logic.io_entities.IUser;
import social_logic.io_entities.IVoiceChannel;

public class DiscordVoiceChannel implements ITextChannel, IVoiceChannel {
    private Session session;
    private long id;

    @Override
    public void moveUser(IUser user) {
        Guild guild = session.getGuild();
        guild.moveVoiceMember(guild.getMemberById(user.getUserId()), getChannel()).queue();
    }

    public VoiceChannel getChannel() { return session.getJDA().getVoiceChannelById(getChannelId()); }

    @Override
    public void sendPlainMessage(String content) { getChannel().sendMessage(content).queue(); }

    @Override
    public long getChannelId() { return id; }

}
