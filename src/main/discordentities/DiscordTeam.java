package discordentities;

import java.util.Set;

import discordentities.checkers.ChannelChecker;
import discordentities.checkers.RoleChecker;
import game.entities.Team;
import languages.TeamLocalization;

public class DiscordTeam extends Team {
    private ChannelChecker voiceChannel;
    private RoleChecker role;

    public DiscordTeam(Set<Long> usersId, TeamLocalization localization) {
        super(usersId, localization);
        voiceChannel = new ChannelChecker(0, 0);
        role = new RoleChecker(0, 0);
    }

    public ChannelChecker getVoiceChannel() { return voiceChannel; }

    public RoleChecker getRole() { return role; }

    public void setGuildId(long guildId) {
        voiceChannel.setGuildId(guildId);
        role.setGuildId(guildId);
    }

    public void setRoleId(long roleId) { role.setRoleId(roleId); }

    public void setVoiceChannel(long channelId) { voiceChannel.setChannelId(channelId); }

    public void setPresident(long userId) {

    }
}
