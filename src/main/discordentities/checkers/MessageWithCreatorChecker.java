package discordentities.checkers;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;

public class MessageWithCreatorChecker extends MessageChecker {
    long creatorId;

    public MessageWithCreatorChecker(Message msg, long creatorId) {
        super(msg);
        this.creatorId = creatorId;
    }

    public MessageWithCreatorChecker(long guildId, long channelId, long messageId, long creatorId) {
        super(guildId, channelId, messageId);
        this.creatorId = creatorId;
    }

    public boolean checkUser(long user) {
        return creatorId == user;
    }

    public boolean checkUser(User user) {
        return checkUser(user.getIdLong());
    }
}
