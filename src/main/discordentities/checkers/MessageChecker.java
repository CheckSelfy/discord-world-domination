package discordentities.checkers;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.react.GenericMessageReactionEvent;

public class MessageChecker extends ChannelChecker {
    long messageId;

    public MessageChecker(Message msg) {
        this(msg.getGuild().getIdLong(), msg.getChannel().getIdLong(), msg.getIdLong());
    }

    public MessageChecker(long guildId, long channelId, long messageId) {
        super(guildId, channelId);
        this.messageId = messageId;
    }

    public boolean check(long guildId, long channelId, long messageId) {
        return check(guildId, channelId) && messageId == this.messageId;
    }

    public boolean check(Message message) {
        return check(message.getGuild().getIdLong(), message.getChannel().getIdLong(), message.getIdLong());
    }

    public boolean check(GenericMessageReactionEvent event) {
        return check(event.getGuild().getIdLong(), event.getChannel().getIdLong(), event.getMessageIdLong())
                && !event.getUser().isBot();
    }

    public long getMessageId() { return messageId; }

    public void setMessageId(long messageId) { this.messageId = messageId; }
}
