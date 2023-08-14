package discordentities;

import socialLogic.IOEntities.IUser;

public class DiscordUser implements IUser {
    private long userId;

    public DiscordUser(long userId) { this.userId = userId; }

    public long getUserId() { return userId; }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (userId ^ (userId >>> 32));
        return result;
    }

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
