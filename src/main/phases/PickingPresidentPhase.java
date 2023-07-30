package phases;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import discordentities.DiscordTeam;
import discordentities.checkers.MessageWithCreatorChecker;
import game.entities.Member;
import game.entities.Team;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.requests.RestAction;
import phases.abstracts.APhase;
import phases.abstracts.IPhase;
import util.Constants;

// Phase 2. This object creates category (voice chats (one per team + general), text chat (general)),
//                  and send poll-pick-president
public class PickingPresidentPhase extends APhase {
    private ArrayList<DiscordTeam> teams;

    public PickingPresidentPhase(JDA jda, List<Set<Long>> uncreatedTeams, MessageWithCreatorChecker msg) {
        teams = new ArrayList<>();
        for (int i = 0; i < uncreatedTeams.size(); i++) {
            if (!uncreatedTeams.get(i).isEmpty()) {
                teams.add(new DiscordTeam(uncreatedTeams.get(i), Constants.teamNames.get(i)));
            }
        }

        createCategoryWithDependantds(jda, msg.getGuildId());
    }

    private void createCategoryWithDependantds(JDA jda, long guildId) {
        Guild guild = jda.getGuildById(guildId);

        guild
            .createCategory(Constants.bundle.getString("game_name"))
            .flatMap(category -> {
                List<RestAction<Void>> actions = new ArrayList<>(teams.size());
                for (Team team: teams) {
                    actions.add(
                        guild.createRole()
                        .setName(team.getName())
                        .setColor(team.getColor()) // created Role
                        .flatMap(role ->  {
                            return addRoles(role, team).and( // added users
                            createTeamVoiceChannel(role, category, team)); // created voice
                        }
                        ));
                }
                return RestAction.allOf(actions).map(listOfVoids -> null);
            }).queue();
                        
    }

    private static RestAction<Role> addRoles(Role role, Team team) {
        Guild guild = role.getGuild();
        List<RestAction<Void>> actions = new ArrayList<>();
        for (Member member: team.getMembers()) {
            actions.add(
                guild.addRoleToMember(role.getJDA().getUserById(member.getUserId()), role)
            );
        }
        return RestAction.allOf(actions).map(listOfVoids -> role); 
    }

    private static RestAction<VoiceChannel> createTeamVoiceChannel(Role role, Category category, Team team) {
        Guild guild = role.getGuild();
        return category
            .createVoiceChannel(team.getName())
            .addRolePermissionOverride(
                guild.getPublicRole().getIdLong(), 
                0,
                Permission.VOICE_CONNECT.getRawValue())
            .addRolePermissionOverride(
                role.getIdLong(), 
                Permission.VOICE_CONNECT.getRawValue(), 
                0);
    }

    @Override
    public IPhase nextPhase(JDA jda) {
        throw new UnsupportedOperationException("Unimplemented method 'nextPhase'");
    }
}