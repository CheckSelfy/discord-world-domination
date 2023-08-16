package discord.checkers;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;

public class RoleChecker extends GuildChecker {
    private long roleId;

    public RoleChecker(Role role) { this(role.getGuild(), role.getIdLong()); }

    public RoleChecker(Guild guild, long roleId) { this(guild.getIdLong(), roleId); }

    public RoleChecker(long guildId, long roleId) {
        super(guildId);
        this.roleId = roleId;
    }

    public long getRoleId() { return roleId; }

    public void setRoleId(long roleId) { this.roleId = roleId; }

    public boolean check(long guildId, long roleId) { return check(guildId) && this.roleId == roleId; }

    public boolean check(Role role) { return check(role.getGuild().getIdLong(), role.getIdLong()); }
}
