package discord.phases;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import app.App;
import discord.DiscordIODevice;
import discord.entities.DiscordMember;
import discord.entities.DiscordTeam;
import discord.entities.DiscordTeamBuilder;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.react.GenericMessageReactionEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;
import net.dv8tion.jda.api.utils.messages.MessageEditData;
import social_logic.Session;
import social_logic.entities.IMember;
import social_logic.phases.handlers_interfaces.ICollectorPhaseEventHandler;
import social_logic.phases.logic.CollectorPhaseLogic;
import util.Constants;
import util.DiscordUtil;
import util.GameUtil;

public class CollectorPhaseHandler extends ADiscordPhaseEventHandler
        implements ICollectorPhaseEventHandler<DiscordTeamBuilder, DiscordTeam> {
    private final List<Set<Long>> teamIds; // teams stored in same order as in Constants.teamNames
    private final CollectorPhaseLogic<DiscordTeamBuilder, DiscordTeam> logic;
    private boolean phaseEnded = false;
    private final Lock lock = new ReentrantLock(true);

    private long pollChannelId;
    private long pollMessageId;
    private long pollCreatorId;

    public CollectorPhaseHandler(Session<DiscordIODevice, IDiscordPhaseEventHandler> session,
            long pollChannelId,
            long pollCreatorId) {
        super(session);

        teamIds = new ArrayList<>(Constants.COUNTRIES_COUNT);
        for (int i = 0; i < Constants.COUNTRIES_COUNT; i++) {
            teamIds.add(new HashSet<>());
        }

        this.pollChannelId = pollChannelId;
        this.pollCreatorId = pollCreatorId;
        this.pollMessageId = getJDA().getTextChannelById(pollChannelId)
                .sendMessage(getCreateMessageData())
                .flatMap(msg -> GameUtil.putCountriesEmoji(msg))
                .complete().getIdLong();

        logic = new CollectorPhaseLogic<>(this, () -> new DiscordTeamBuilder());
        System.out.println("Started");

        scheduleEnd();
        scheduleBeforeEnd(() -> System.out.println("5 seconds to go!"), 5_000); // TODO: Example. Remove it
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        if (event.getChannel().getIdLong() != pollChannelId || event.getMessage().getIdLong() != pollMessageId) {
            return;
        }

        if (!event.getButton().getId().equals("start_game")) {
            return;
        }

        if (event.getUser().getIdLong() != pollCreatorId) {
            event.reply(Constants.bundle.getString("start_not_creator")).setEphemeral(true).queue();
            return;
        }

        event.deferEdit().queue();

        lock.lock();
        if (!hasRepeaterUsers()) {
            MessageEditData changes = new MessageEditBuilder().setComponents().setEmbeds(buildEmbedWithUsers(false))
                    .build();
            event.getHook().editMessageById(pollMessageId, changes).flatMap(msg -> msg.clearReactions())
                    .queue();

            logic.collectMembers(idsToMembers());
        } else {
            event.getHook().sendMessage("Each user should be in one team only").setEphemeral(true).queue();
        }
        lock.unlock();
    }

    private ArrayList<Set<IMember>> idsToMembers() {
        ArrayList<Set<IMember>> result = new ArrayList<>(teamIds.size());
        for (Set<Long> s : teamIds) {
            HashSet<IMember> newHashSet = new HashSet<>(s.size());
            for (long userId : s) {
                newHashSet.add(new DiscordMember(userId));
            }
            result.add(newHashSet);
        }
        return result;
    }

    private MessageEmbed buildEmbedWithUsers(boolean addEmptyFields) {
        EmbedBuilder builder = Constants.getEmptyEmbedBuilder();

        for (int i = 0; i < Constants.COUNTRIES_COUNT; i++) {
            StringJoiner joiner = new StringJoiner(", ");
            for (Long user : teamIds.get(i))
                if (getJDA().getSelfUser().getIdLong() != user) {
                    joiner.add(DiscordUtil.getDiscordMentionTag(user));
                }

            if (addEmptyFields || joiner.length() != 0) {
                builder.addField(Constants.teamNames.get(i).getFullName(), joiner.toString(), false);
            }
        }

        return builder.build();
    }

    @Override
    public void onGenericMessageReaction(GenericMessageReactionEvent event) {
        if (event.getChannel().getIdLong() != pollChannelId || event.getMessageIdLong() != pollMessageId) {
            return;
        }

        if (event.getUser().isBot()) {
            return;
        }

        int team = getTeamByEmoji(event.getEmoji());
        if (team == -1) {
            return;
        }

        lock.lock();
        if (phaseEnded) {
            return;
        }

        if (event instanceof MessageReactionAddEvent) {
            teamIds.get(team).add(event.getUserIdLong());
        } else {
            teamIds.get(team).remove(event.getUserIdLong());
        }

        updateMessage();
        lock.unlock();
    }

    private boolean hasRepeaterUsers() {
        Set<Long> users = new HashSet<>();
        for (Set<Long> team : teamIds) {
            for (Long id : team) {
                if (users.contains(id)) {
                    return true;
                }
                users.add(id);
            }
        }
        return false;
    }

    private static int getTeamByEmoji(Emoji emoji) {
        for (int i = 0; i < Constants.COUNTRIES_COUNT; i++) {
            if (Constants.teamNames.get(i).getEmoji().equals(emoji)) {
                return i;
            }
        }

        return -1;
    }

    private static final Button button = Button.of(ButtonStyle.SUCCESS, "start_game",
            Constants.bundle.getString("start_game_button"),
            Emoji.fromFormatted(Constants.bundle.getString("start_emoji_button")));

    private MessageCreateData getCreateMessageData() {
        return new MessageCreateBuilder()
                .setEmbeds(buildEmbedWithUsers(true))
                .setActionRow(button.asEnabled()).build();
    }

    private MessageEditData getEditMessageData() {
        return new MessageEditBuilder()
                .setEmbeds(buildEmbedWithUsers(true))
                .setActionRow(hasRepeaterUsers() ? button.asDisabled() : button.asEnabled()).build();
    }

    private void updateMessage() {
        getJDA().getTextChannelById(pollChannelId)
                .editMessageById(pollMessageId, getEditMessageData())
                .queue();
    }

    @Override
    public void nextPhase(List<DiscordTeamBuilder> builders) {
        phaseEnded = true;
        session.setPhaseHandler(new PresidentPickingPhaseHandler(session, builders));
    }

    // deleting game session
    @Override
    public void phaseEnding() {
        System.out.println("Ended.");
        getJDA().getTextChannelById(pollChannelId).deleteMessageById(pollMessageId).complete();
        App.removeSession(session);
        getJDA().removeEventListener(session.getIODevice());
    }

    @Override
    public int getDurationInMilliseconds() { return 1000 * 60 * 15; }

}
