import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import net.dv8tion.jda.api.events.message.react.GenericMessageReactionEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;
import net.dv8tion.jda.api.utils.messages.MessageEditData;

public class ListenerStartMessage extends ListenerAdapter {
    final long messageId;
    final long textChannelId;
    final long creatorOfMessage;

    Map<Emoji, Set<Long>> teams; // <emoji, user_set>

    public ListenerStartMessage(long messageId, long textChannelId, long creatorOfMessage) {
        this.messageId = messageId;
        this.textChannelId = textChannelId;
        this.creatorOfMessage = creatorOfMessage;

        teams = new HashMap<>(Constants.COUNTRIES_COUNT);
        for (Emoji teamEmoji : Constants.EMOJIS_TO_COUNTRY.keySet())
            teams.put(teamEmoji, new HashSet<>());
    }

    public boolean needChanges(GenericMessageReactionEvent event) {
        if (event.getMessageIdLong() != messageId || event.getChannel().getIdLong() != textChannelId)
            return false;

        if (event.getUser().isBot() || event.getUser().isSystem())
            return false;

        // TODO add support for custom emojis.

        return true;
    }

    @Override
    public void onMessageReactionAdd(MessageReactionAddEvent event) {
        if (!needChanges(event))
            return;

        teams.get(event.getEmoji()).add(event.getUserIdLong());

        updateMessage(event.getChannel());
    }

    @Override
    public void onMessageReactionRemove(MessageReactionRemoveEvent event) {
        if (!needChanges(event))
            return;

        teams.get(event.getEmoji()).remove(event.getUserIdLong());

        updateMessage(event.getChannel());
    }

    private record StateOfMessage(EmbedBuilder builder, boolean canStart) {
        // TODO bad naming
    }

    // Returns
    private StateOfMessage getStateOfMessage() {
        EmbedBuilder builder = new EmbedBuilder()
                .setTitle(Constants.bundle.getString("game_name"))
                .setTimestamp(Instant.now());

        Set<Long> uniqueUsers = new HashSet<>();
        boolean canStart = true;

        for (Map.Entry<Emoji, Set<Long>> entry : teams.entrySet()) {
            String countryName = Constants.getFullNameOfCountry(entry.getKey());

            Set<Long> usersInTeam = entry.getValue();

            StringJoiner value = new StringJoiner(", ");

            for (Long userId : usersInTeam) {
                canStart &= !(uniqueUsers.contains(userId));
                uniqueUsers.add(userId);

                value.add("<@" + userId + ">"); // maybe <@!userId>
            }

            builder.addField(countryName, value.toString(), false);
        }

        return new StateOfMessage(builder, canStart);
    }

    private void updateMessage(MessageChannelUnion ch) {
        StateOfMessage state = getStateOfMessage();
        EmbedBuilder builder = state.builder;

        builder.setDescription("Waiting for people");

        Button startButton = Button.of(ButtonStyle.PRIMARY, "start_button",
                Constants.bundle.getString("start_game_button"),
                Emoji.fromFormatted(Constants.bundle.getString("random_emoji")));

        ch.editMessageEmbedsById(messageId, builder.build())
                .setComponents(ActionRow.of(
                        // startButton.asEnabled()))
                        state.canStart ? startButton.asEnabled() : startButton.asDisabled()))
                .queue();
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        if (event.getMessageIdLong() != messageId || event.getChannel().getIdLong() != textChannelId)
            return;

        if (event.getUser().getIdLong() != creatorOfMessage) {
            event.reply(
                    Constants.bundle.getString("start_not_creator"))
                    .setEphemeral(true)
                    .queue();
            return;
        }

        startGame(event);
    }

    @Override
    public void onMessageDelete(MessageDeleteEvent event) {
        if (event.getMessageIdLong() == messageId && event.getChannel().getIdLong() == textChannelId) {
            event.getJDA().removeEventListener(this);
        }
    }

    // only changes messages and create a game
    private void startGame(ButtonInteractionEvent event) {
        event.deferEdit().queue();

        EmbedBuilder builder = getStateOfMessage().builder;

        builder.setDescription("Game started!");

        MessageEditData changesToMessage = new MessageEditBuilder()
                .setComponents()
                .setEmbeds(builder.build())
                .build();

        event.getHook()
                .editMessageById(messageId, changesToMessage)
                .queue();

        event.getMessage().clearReactions().queue();

        event.getJDA()
                .removeEventListener(this);

        event.getJDA()
                .addEventListener(new GameCommunicator(event.getGuild().getIdLong(), teams, event.getJDA()));
    }
}