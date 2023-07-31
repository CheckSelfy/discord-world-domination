package phases;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import discordentities.DiscordTeam;
import discordentities.checkers.MessageWithPrivilegeUserChecker;
import game.entities.Member;
import game.entities.Team;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;
import net.dv8tion.jda.api.utils.messages.MessageEditData;
import phases.abstracts.APhase;
import phases.abstracts.IPhase;
import util.Constants;

// Phase 2. This object creates category (voice chats (one per team + general), text chat (general)),
//                  and send poll-pick-president
public class PickingPresidentPhase extends APhase {
    private ArrayList<DiscordTeam> teams;
    private AtomicInteger remainingPolls;

    public PickingPresidentPhase(JDA jda, List<Set<Long>> uncreatedTeams, MessageWithPrivilegeUserChecker msg) {
        teams = new ArrayList<>();
        for (int i = 0; i < uncreatedTeams.size(); i++) {
            if (!uncreatedTeams.get(i).isEmpty()) {
                teams.add(new DiscordTeam(uncreatedTeams.get(i), Constants.teamNames.get(i)));
            }
        }

        remainingPolls = new AtomicInteger(teams.size());

        createCategoryWithDependantds(jda, msg.getGuildId()).flatMap(o -> createPolls(jda)).queue();
    }

    private RestAction<Object> createCategoryWithDependantds(JDA jda, long guildId) {
        Guild guild = jda.getGuildById(guildId);

        return guild
                .createCategory(Constants.bundle.getString("game_name"))
                .flatMap(category -> {
                    List<RestAction<Void>> actions = new ArrayList<>(teams.size());
                    for (DiscordTeam team : teams) {
                        actions.add(
                                guild.createRole()
                                        .setName(team.getName())
                                        .setColor(team.getColor()) // created Role
                                        .flatMap(role -> {
                                            team.setGuildId(role.getGuild().getIdLong());
                                            team.setRoleId(role.getIdLong());

                                            return addRoles(role, team) // added users
                                                    .and(createTeamVoiceChannel(role, category, team)); // created voice
                                        }));
                    }
                    return RestAction.allOf(actions).map(listOfVoids -> null);
                });

    }

    private static RestAction<Role> addRoles(Role role, Team team) {
        Guild guild = role.getGuild();
        List<RestAction<Void>> actions = new ArrayList<>();
        for (Member member : team.getMembers()) {
            actions.add(
                    guild.addRoleToMember(role.getJDA().getUserById(member.getUserId()), role));
        }
        return RestAction.allOf(actions).map(listOfVoids -> role);
    }

    private static RestAction<Void> createTeamVoiceChannel(Role role, Category category, DiscordTeam team) {
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
                        0)
                .map(vc -> {
                    team.getVoiceChannel().setGuildId(guild.getIdLong());
                    team.getVoiceChannel().setChannelId(vc.getIdLong());
                    return null;
                });
    }

    static String pollButtonId = "pickPresident";

    private RestAction<Void> createPolls(JDA jda) {
        MessageCreateBuilder builder = new MessageCreateBuilder()
                .setContent("Pick your presidents! [DEBUG]");
        List<RestAction<Void>> actions = new ArrayList<>(teams.size());
        for (int i = 0; i < teams.size(); i++) {
            DiscordTeam team = teams.get(i);
            builder.setActionRow(Button.of(ButtonStyle.PRIMARY, "pickPresident" + i, "Random!"));
            actions.add(
                    jda.getVoiceChannelById(team.getVoiceChannel().getChannelId())
                            .sendMessage(builder.build())
                            .flatMap(msg -> {
                                team.getColor();
                                return msg
                                        .addReaction(
                                                Emoji.fromFormatted(
                                                        Constants.bundle.getString("pick_president_emoji")));
                            }));
        }
        return RestAction.allOf(actions).map(l -> null);
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        if (!event.getButton().getId().startsWith("pickPresident"))
            return;
        int indexOfTeam = Integer.parseInt(event.getButton().getId().substring(pollButtonId.length()));
        DiscordTeam team = teams.get(indexOfTeam);
        event.deferEdit().queue();
        event.getChannel().asVoiceChannel().retrieveMessageById(event.getMessageId())
                .flatMap(msg -> msg.getReaction(Emoji.fromFormatted(Constants.bundle.getString("pick_president_emoji"))).retrieveUsers())
                .map(listUsers -> {
                    int size = listUsers.size() - 1; // remove selfuser
                    if (size == 0) {
                        size += team.getMembers().size();
                    }
                    int pickedUserNumber = new Random(event.getIdLong()).nextInt(size);
                    return listUsers.get(pickedUserNumber);
                }).flatMap(user -> {
                    team.setPresident(user.getIdLong());
                    remainingPolls.decrementAndGet();
                    MessageEditData edit = new MessageEditBuilder()
                            .setContent("Your president: <@" + user.getIdLong() + ">")
                            .setComponents()
                            .build();
                    return event.getHook().editMessageById(event.getMessageId(), edit);
                }).flatMap(message -> message.clearReactions())
                .map(v -> {
                    if (remainingPolls.get() == 0) {
                        changeToNextPhase(event.getJDA());
                    }
                    return null;
                }).queue();
    }

    @Override
    public IPhase nextPhase(JDA jda) {
        return new MainPhase();
    }
}