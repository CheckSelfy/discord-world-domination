package discord.entities;

import social_logic.entities.IMember;

public class DiscordMember implements IMember {
    private final long userId;

    public DiscordMember(long userId) { this.userId = userId; }

    @Override
    public long getID() { return userId; }
}