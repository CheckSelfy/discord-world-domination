package discordentities.checkers;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;

public class MessageWithPrivilegeUserChecker extends MessageChecker {
    private long userId;

    public MessageWithPrivilegeUserChecker(Message msg, long userId) {
        super(msg);
        this.userId = userId;
    }

    public MessageWithPrivilegeUserChecker(long guildId, long channelId, long messageId, long userId) {
        super(guildId, channelId, messageId);
        this.userId = userId;
    }

    public boolean checkUser(long userId) {
        return this.userId == userId;
    }

    public boolean checkUser(User user) {
        return checkUser(user.getIdLong());
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public void setUserId(User user) {
        setUserId(user.getIdLong());
    }
}
