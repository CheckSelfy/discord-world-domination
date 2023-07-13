import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.react.GenericMessageReactionEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;
import net.dv8tion.jda.api.utils.messages.MessageEditData;
import util.Pair;

public class ListenerStartMessage extends ListenerAdapter {
    final long messageId;
    final long textChannelId;
    final long creatorOfMessage;

    Map<String, Set<Long>> teams;

    public ListenerStartMessage(long messageId, long textChannelId, long creatorOfMessage) {
        this.messageId = messageId;
        this.textChannelId = textChannelId;
        this.creatorOfMessage = creatorOfMessage;

        teams = new HashMap<>(Constants.COUNTRIES_COUNT);
        for (String teamName : Constants.COUNTRIES)
            teams.put(teamName, new HashSet<>());
    }

    public boolean needChanges(GenericMessageReactionEvent event) {
        if (event.getMessageIdLong() != messageId || event.getChannel().getIdLong() != textChannelId)
            return false;

        if (event.getUser().isBot() || event.getUser().isSystem())
            return false;

        return true;
    }

    @Override
    public void onMessageReactionAdd(MessageReactionAddEvent event) {
        if (!needChanges(event))
            return;

        String nameOfCountry = Constants.EMOJI_TO_COUNTRY.get(event.getEmoji().getAsReactionCode());
        teams.get(nameOfCountry).add(event.getUserIdLong());

        updateMessage(event.getChannel());
    }

    @Override
    public void onMessageReactionRemove(MessageReactionRemoveEvent event) {
        if (!needChanges(event))
            return;

        String nameOfCountry = Constants.EMOJI_TO_COUNTRY.get(event.getEmoji().getAsReactionCode());
        teams.get(nameOfCountry).remove(event.getUserIdLong());

        updateMessage(event.getChannel());
    }
    
    private class StateOfStartButton {

    }

    // Returns 
    private Pair<List<String>, Boolean> getEmbedBuilderWithTeams() {
        EmbedBuilder builder = new EmbedBuilder()
                .setAuthor("Artyom T, @CheckSelf")
                .setTitle(Constants.bundle.getString("game_name"));

        Set<Long> uniqueUsers = new HashSet<>();
        boolean canStart = true;

        for (Map.Entry<String, Set<Long>> entry : teams.entrySet()) {
            String countryName = entry.getKey();
            countryName = Constants.bundle.getString(countryName) + " "
                    + Constants.COUNTRY_TO_EMOJI.get(countryName);
            Set<Long> usersInTeam = entry.getValue();

            StringJoiner value = new StringJoiner(", ");

            for (Long user : usersInTeam) {
                canStart &= !(uniqueUsers.contains(user));
                uniqueUsers.add(user);

                value.add("<@" + user + ">");
            }
            builder.addField(countryName, value.toString(), false);
        }

        return new Pair<List<String>, Boolean>(builder, canStart);
    }

    private void updateMessage(MessageChannelUnion ch) {
        Button startButton = Button.of(ButtonStyle.PRIMARY, "start_button",
                Constants.bundle.getString("start_game_button"),
                Emoji.fromUnicode(Constants.bundle.getString("random_emoji")));

        ch.editMessageEmbedsById(messageId, builder.build())
                .setComponents(ActionRow.of(
                        canStart ? startButton.asEnabled() : startButton.asDisabled()))
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

        event.deferEdit().queue();
        
        MessageEditData changesToMessage = new MessageEditBuilder()
            .setComponents()
            .setContent(Constants.bundle.getString("start_created_game_message"))
            .build();

        event.getHook()
            .editMessageById(messageId, changesToMessage)
            .queue();
        
        event.getJDA().removeEventListener(this);
    }
}