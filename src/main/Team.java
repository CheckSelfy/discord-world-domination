import java.util.Set;

public record Team(
        Set<Long> users,
        long roleId,
        long guildId,
        long voiceChannelId) {
}
