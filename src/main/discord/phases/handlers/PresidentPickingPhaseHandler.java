package discord.phases.handlers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

import discord.Session;
import discord.phases.PresidentPickingPhaseLogic;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.react.GenericMessageReactionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;
import net.dv8tion.jda.api.utils.messages.MessageEditData;
import social_logic.entities.Team;
import social_logic.io_entities.IUser;
import util.Constants;
import util.DiscordUtil;

public class PresidentPickingPhaseHandler extends APhaseEventHandler {
    private ArrayList<Team> teams;
    private ArrayList<AtomicBoolean> readinness;

    public PresidentPickingPhaseHandler(Session session) {
        super(session, new PresidentPickingPhaseLogic());
        teams = new ArrayList<>();
        for (int i = 0; i < uncreatedTeams.size(); i++) {
            if (!uncreatedTeams.get(i).isEmpty()) {
                teams.add(new DiscordTeam(uncreatedTeams.get(i),
                        Constants.teamNames.get(i)));
            }
        }

        readinness = new ArrayList<>(teams.size());
        for (int i = 0; i < readinness.size(); i++) {
            readinness.set(i, new AtomicBoolean(false));
        }

        createCategoryWithDependantds(session.getGuildId()).flatMap(o -> createPolls()).queue();
    }

    private RestAction<? extends Object> createCategoryWithDependantds(long guildId) {
        Guild guild = session.getJDA().getGuildById(guildId);

        return guild.createCategory(Constants.bundle.getString("game_name"))
                .flatMap(category -> guild
                        .modifyCategoryPositions()
                        .selectPosition(category)
                        .moveTo(1)
                        .map(v -> category))
                .flatMap(category -> {
                    List<RestAction<Void>> actions = new ArrayList<>(teams.size());
                    for (Team team : teams) {
                        actions.add(
                                guild.createRole()
                                        .setName(team.getLocalization().getName())
                                        .setColor(team.getLocalization().getColor()) // created Role
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
        for (IUser member : team.getMembers()) {
            actions.add(guild.addRoleToMember(session.getJDA().getUserById(member.getUserId()), role));
        }
        return RestAction.allOf(actions).map(listOfVoids -> role);
    }

    private RestAction<Void> createPolls() {
        MessageCreateBuilder builder = new MessageCreateBuilder()
                .setContent(Constants.bundle.getString("pick_president_message"));
        List<RestAction<Void>> actions = new ArrayList<>(teams.size());
        for (int i = 0; i < teams.size(); i++) {
            Team team = teams.get(i);
            builder.setActionRow(Button.of(ButtonStyle.PRIMARY, pollButtonId + i,
                    Constants.bundle.getString("pick_president_button")));
            actions.add(session.getJDA().getVoiceChannelById(team.getChannel().getVoiceChannel().getChannelId())
                    .sendMessage(builder.build())
                    .flatMap(msg -> {
                        team.getLocalization().getColor();
                        return msg
                                .addReaction(
                                        Emoji.fromFormatted(
                                                Constants.bundle.getString("pick_president_emoji")));
                    }));
        }
        return RestAction.allOf(actions).map(l -> null);
    }

    private static final String pollButtonId = "pickPresident";

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        System.out.println("Button '" + event.getButton().getId() + "' clicked");

        if (!event.getButton().getId().startsWith(pollButtonId))
            return;

        int indexOfTeam = Integer.parseInt(event.getButton().getId().substring(pollButtonId.length()));

        if (!readinness.get(indexOfTeam).compareAndSet(false, true)) {
            event.reply(Constants.bundle.getString("pick_president_button_already_clicked"));
            return;
        }

        Team team = teams.get(indexOfTeam);
        // System.out.println("team = " + team.getLocalization().getName());
        event.deferEdit().queue();
        event.getChannel().asVoiceChannel().retrieveMessageById(event.getMessageId())
                .flatMap(msg -> msg.getReaction(Emoji.fromFormatted(Constants.bundle.getString("pick_president_emoji")))
                        .retrieveUsers())
                .flatMap(listUsers -> {
                    int size = listUsers.size() - 1; // remove bot vote
                    long userId;
                    if (size == 0) {
                        size = team.getMembers().size();
                        Iterator<IUser> iter = team.getMembers().iterator();
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
                    MessageEditData edit = new MessageEditBuilder()
                            .setContent(Constants.bundle.getString("pick_president_final_message")
                                    + DiscordUtil.getDiscordMentionTag(userId))
                            .setComponents()
                            .build();
                    return event.getHook().editMessageById(event.getMessageId(), edit);
                }).flatMap(msg -> msg.clearReactions()).map(v -> {
                    boolean isAllTrue = true;
                    for (int i = 0; i < readinness.size(); i++) {
                        isAllTrue &= readinness.get(i).get();
                    }
                    if (isAllTrue) {
                        // SocialLogic
                        // - Game
                        nextPhase();
                    }
                    return null;
                }).queue();
    }

    @Override
    public void onGenericMessageReaction(GenericMessageReactionEvent event) {

    }

    public void nextPhase() { /* session.setPhase(new TalkingPhase(session)); */ }
}
