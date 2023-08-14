package phases;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.StringJoiner;

import discordentities.checkers.MessageWithPrivilegeUserChecker;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import net.dv8tion.jda.api.events.message.react.GenericMessageReactionEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;
import net.dv8tion.jda.api.utils.messages.MessageEditData;
import phases.abstracts.IPhase;
import phases.abstracts.PhaseWithTimer;
import util.Constants;
import util.DiscordUtil;
import util.GameUtil;

// Phase 1. This object collects people for upcoming game
public class CollectorPhase extends PhaseWithTimer {
    MessageWithPrivilegeUserChecker message;
    ArrayList<Set<Long>> teams;

    public CollectorPhase(SlashCommandInteractionEvent event) {
        super(event.getJDA());
        teams = new ArrayList<>(Constants.COUNTRIES_COUNT);
        for (int i = 0; i < Constants.COUNTRIES_COUNT; i++) {
            teams.add(new HashSet<>());
        }

        event.deferReply().queue();

        event.getHook()
                .sendMessageEmbeds(
                        Constants.getEmptyEmbedBuilder()
                                .setDescription(Constants.bundle.getString("start_embedDescription")).build())
                .flatMap(msg -> {
                    message = new MessageWithPrivilegeUserChecker(msg, event.getUser().getIdLong());
                    return GameUtil.putCountriesEmoji(msg);
                }).queue();
        
        schedule(this::timerEndedMethod);
    }

    public static int getTeamByEmoji(Emoji emoji) {
        for (int i = 0; i < Constants.COUNTRIES_COUNT; i++) {
            if (Constants.teamNames.get(i).getEmoji().equals(emoji)) {
                return i;
            }
        }

        return -1;
    }

    @Override
    public void onGenericMessageReaction(GenericMessageReactionEvent event) {
        if (!message.check(event)) {
            return;
        }

        int team = getTeamByEmoji(event.getEmoji());
        if (team == -1)
            return;

        if (event instanceof MessageReactionAddEvent)
            teams.get(team).add(event.getUserIdLong());
        else
            teams.get(team).remove(event.getUserIdLong());

        updateMessage();
    }

    private boolean usersRepeat() {
        Set<Long> checkedUsers = new HashSet<>();
        for (Set<Long> team : teams) {
            for (Long user : team) {
                if (checkedUsers.contains(user))
                    return true;
                checkedUsers.add(user);
            }
        }
        return false;
    }

    private MessageEmbed buildEmbedWithUsers(boolean addEmptyFields) {
        EmbedBuilder builder = Constants.getEmptyEmbedBuilder();

        for (int i = 0; i < Constants.COUNTRIES_COUNT; i++) {
            StringJoiner joiner = new StringJoiner(", ");
            for (Long user : teams.get(i))
                joiner.add(DiscordUtil.getDiscordMentionTag(user));

            if (addEmptyFields || joiner.length() != 0)
                builder.addField(Constants.teamNames.get(i).getFullName(), joiner.toString(), false);
        }

        return builder.build();
    }

    private void updateMessage() {
        Button button = Button.of(ButtonStyle.SUCCESS, "start_game", Constants.bundle.getString("start_game_button"),
                Emoji.fromFormatted(Constants.bundle.getString("start_emoji_button")));

        getJDA().getTextChannelById(message.getChannelId())
                .editMessageEmbedsById(message.getMessageId(), buildEmbedWithUsers(true))
                .setActionRow(button.asEnabled()).queue();
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        if (!event.getButton().getId().equals("start_game") || // not start button
                !message.check(event.getMessage())) {
            return;
        }

        if (!message.checkUser(event.getUser())) {
            event.reply(Constants.bundle.getString("start_not_creator")).setEphemeral(true).queue();
            return;
        }

        // blocking the button
        event.deferEdit().queue();
        MessageEditData changes = new MessageEditBuilder().setComponents().setEmbeds(buildEmbedWithUsers(false))
                .build();

        event.getHook().editMessageById(message.getMessageId(), changes) // applying changes
                .flatMap(msg -> msg.clearReactions()).queue();

        changeToNextPhase();
    }

    @Override
    public IPhase nextPhase() {
        return new PickingPresidentPhase(getJDA(), teams, message);
    }

    @Override
    public void onMessageDelete(MessageDeleteEvent e) {
        if (message.check(e.getGuild().getIdLong(), e.getChannel().getIdLong(), e.getMessageIdLong())) {
            getJDA().removeEventListener(this); // game deleted.
        }
    }

    @Override
    public int getDurationInSeconds() {
        return Integer.parseInt(Constants.properties.getProperty("collectorPhaseDurationInSeconds"));
    }

    public void timerEndedMethod() { 
        getJDA().getTextChannelById(message.getChannelId()).deleteMessageById(message.getMessageId()).queue();
    }
}
