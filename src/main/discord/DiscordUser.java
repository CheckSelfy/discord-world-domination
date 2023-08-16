package discord;

import social_logic.io_entities.IUser;

public class DiscordUser implements IUser {
    private long userId;

    public DiscordUser(long userId) { this.userId = userId; }

    @Override
    public long getUserId() { return userId; }

    @Override
    public int hashCode() { return (int) userId; }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        DiscordUser other = (DiscordUser) obj;
        if (userId != other.userId)
            return false;
        return true;
    }

    @Override
    public String toString() { return "<@" + userId + ">"; }

}
