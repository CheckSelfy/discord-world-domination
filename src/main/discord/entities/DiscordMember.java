package discord.entities;

import social_logic.entities.IMember;

public class DiscordMember implements IMember {
    private final long userId;

    public DiscordMember(long userId) { this.userId = userId; }

    @Override
    public long getID() { return userId; }

    @Override
    public int hashCode() { return (int) getID(); }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj == null) {
            return false;
        } else if (obj instanceof DiscordMember other) {
            if (getID() == other.getID()) {
                return true;
            }
        }
        return false;
    }

}
