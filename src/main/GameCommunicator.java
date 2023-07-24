import java.util.Map;
import java.util.Set;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class GameCommunicator extends ListenerAdapter {
    final long guildId;
    final Map<Emoji, Set<Long>> teams;

    public GameCommunicator(long guildId, Map<Emoji, Set<Long>> teams, JDA jda) {
        this.guildId = guildId;
        this.teams = teams;

        Category category = jda.getGuildById(guildId).createCategory("Global domination").complete();

        for (Map.Entry<Emoji, Set<Long>> team: teams.entrySet()) {
            Emoji teamEmoji = team.getKey();
            Set<Long> users = team.getValue();

            if (users.isEmpty())
                continue;

            Role role = jda
                .getGuildById(guildId)
                .createRole()
                .setName(Constants.getFullNameOfCountry(teamEmoji))
                .setColor(Constants.getTeamColor(teamEmoji))
                .complete();

            for (long userId: team.getValue()) {
                category.getGuild().addRoleToMember(User.fromId(userId), role).queue();
            }

            category
                .createVoiceChannel(Constants.getFullNameOfCountry(teamEmoji))
                .addRolePermissionOverride(
                    jda.getGuildById(guildId).getPublicRole().getIdLong(), 
                    0,
                    Permission.VOICE_CONNECT.getRawValue()
                    )
                .addRolePermissionOverride(
                    role.getIdLong(), 
                    Permission.VOICE_CONNECT.getRawValue(), 
                    0)
                .queue();
        }
    }
}