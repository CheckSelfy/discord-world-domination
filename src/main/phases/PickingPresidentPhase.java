package phases;

import java.util.ArrayList;
import java.util.Iterator;
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
import net.dv8tion.jda.api.entities.User;
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
import util.DiscordUtil;

// Phase 2. This object creates category (voice chats (one per team + general), text chat (general)),
//                  and send poll-pick-president

// TODO. It's bad idea to use atomicinteger counter. Suppose that two players of same team click the button -> game is broken.
//                                                   Maybe it would be better to create array of booleans.
public class PickingPresidentPhase extends APhase {
    private ArrayList<DiscordTeam> teams;

    public PickingPresidentPhase(JDA jda, List<Set<Long>> uncreatedTeams, MessageWithPrivilegeUserChecker msg) {
        super(jda);
        teams = new ArrayList<>();
        for (int i = 0; i < uncreatedTeams.size(); i++) {
            if (!uncreatedTeams.get(i).isEmpty()) {
                teams.add(new DiscordTeam(uncreatedTeams.get(i), Constants.teamNames.get(i)));
            }
        }

        remainingPolls = new AtomicInteger(teams.size());

        createCategoryWithDependantds(msg.getGuildId()).flatMap(o -> createPolls()).queue();
    }

    private RestAction<? extends Object> createCategoryWithDependantds(long guildId) {
        Guild guild = getJDA().getGuildById(guildId);

        return guild
                .createCategory(Constants.bundle.getString("game_name"))
                .flatMap(category -> guild
                        .modifyCategoryPositions()
                        .selectPosition(category)
                        .moveTo(1)
                        .map(v -> category))
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
                    return RestAction.allOf(actions);
                });

    }

    private RestAction<Role> addRoles(Role role, Team team) {
        Guild guild = role.getGuild();
        List<RestAction<Void>> actions = new ArrayList<>();
        for (Member member : team.getMembers()) {
            actions.add(
                    guild.addRoleToMember(getJDA().getUserById(member.getUserId()), role));
        }
        return RestAction.allOf(actions).map(listOfVoids -> role);
    }

    private RestAction<? extends Object> createTeamVoiceChannel(Role role, Category category, DiscordTeam team) {
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
                    return vc;
                });
    }

    private AtomicInteger remainingPolls;
    static String pollButtonId = "pickPresident";

    private RestAction<Void> createPolls() {
        MessageCreateBuilder builder = new MessageCreateBuilder()
                .setContent("Pick your presidents! [DEBUG]");
        List<RestAction<Void>> actions = new ArrayList<>(teams.size());
        for (int i = 0; i < teams.size(); i++) {
            DiscordTeam team = teams.get(i);
            builder.setActionRow(Button.of(ButtonStyle.PRIMARY, pollButtonId + i, "Pick! [DEBUG]"));
            actions.add(
                    getJDA().getVoiceChannelById(team.getVoiceChannel().getChannelId())
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
        System.out.println("Button '" + event.getButton().getId() + "' clicked");

        if (!event.getButton().getId().startsWith("pickPresident"))
            return;

        int indexOfTeam = Integer.parseInt(event.getButton().getId().substring(pollButtonId.length()));
        DiscordTeam team = teams.get(indexOfTeam);
        System.out.println("team = " + team.getName());
        event.deferEdit().queue();
        event.getChannel().asVoiceChannel().retrieveMessageById(event.getMessageId())
                .flatMap(msg -> msg.getReaction(Emoji.fromFormatted(Constants.bundle.getString("pick_president_emoji")))
                        .retrieveUsers())
                .flatMap(listUsers -> {
                    int size = listUsers.size() - 1; // remove selfuser
                    long userId;
                    if (size == 0) {
                        size = team.getMembers().size();
                        Iterator<Member> iter = team.getMembers().iterator();
                        for (int i = 0; i < size - 1; i++)
                            iter.next();
                        userId = iter.next().getUserId();
                    } else {
                        listUsers.removeIf(u -> u.isBot());
                        int pickedUserNumber = new Random(event.getIdLong()).nextInt(size);
                        User user = listUsers.get(pickedUserNumber);
                        userId = user.getIdLong();
                    }
                    team.setPresident(userId);
                    remainingPolls.decrementAndGet();
                    MessageEditData edit = new MessageEditBuilder()
                            .setContent("Your president [DEBUG]: " + DiscordUtil.getDiscordMentionTag(userId))
                            .setComponents()
                            .build();
                    return event.getHook().editMessageById(event.getMessageId(), edit);
                }).flatMap(msg -> msg.clearReactions()).map(v -> {
                    if (remainingPolls.get() == 0) {
                        changeToNextPhase();
                    }
                    return null;
                }).queue();
    }

    @Override
    public IPhase nextPhase() {
        return new TalkingPhase(getJDA(), teams);
    }
}