package discordentities;

// Guild -> SocialLogic
public class DiscordGuild {
    private long id;

    public DiscordGuild(long id) { this.id = id; }

    public long getId() { return id; }

    @Override
    public int hashCode() { return (int) id; }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        DiscordGuild other = (DiscordGuild) obj;
        if (id != other.id)
            return false;
        return true;
    }
}
