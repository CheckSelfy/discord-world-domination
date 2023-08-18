package discord.phases;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.StringJoiner;

import discord.DiscordIODevice;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.react.GenericMessageReactionEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;
import net.dv8tion.jda.api.utils.messages.MessageEditData;
import social_logic.Session;
import social_logic.phases.handlers_interfaces.ICollectorPhaseEventHandler;
import util.Constants;
import util.DiscordUtil;

public class CollectorPhaseHandler extends ADiscordPhaseEventHandler
        implements ICollectorPhaseEventHandler {
    private final ArrayList<Set<Long>> teams;
    // store msg;

    public CollectorPhaseHandler(Session<DiscordIODevice, IDiscordPhaseEventHandler> session) {
        super(session);
        teams = new ArrayList<>(Constants.COUNTRIES_COUNT);
        for (int i = 0; i < Constants.COUNTRIES_COUNT; i++) {
            teams.add(new HashSet<>());
        }

        // TODO: create msg !!!!!!!!!!!!

        // create logic
    }

    @Override
    public void nextPhase() { session.setPhase(new PresidentPickingPhaseHandler(session)); }

    /* some method */ {
        // decided to change to next phase
        nextPhase();
    }

    //
    //
    //

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        if (!event.getButton().getId().equals("start_game")) {
            return;
        }

        if (!message.checkUser(event.getUser())) {
            event.reply(Constants.bundle.getString("start_not_creator")).setEphemeral(true).queue();
            return;
        }

        event.deferEdit().queue();
        MessageEditData changes = new MessageEditBuilder().setComponents().setEmbeds(buildEmbedWithUsers(false))
                .build();

        event.getHook().editMessageById(message.getMessageId(), changes).flatMap(msg -> msg.clearReactions()).queue();

        // call logic method
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

    //
    //

    @Override
    public void onGenericMessageReaction(GenericMessageReactionEvent event) {
        int team = getTeamByEmoji(event.getEmoji());
        if (team == -1)
            return;

        if (event instanceof MessageReactionAddEvent)
            teams.get(team).add(event.getUserIdLong());
        else
            teams.get(team).remove(event.getUserIdLong());

        updateMessage();
    }

    private static int getTeamByEmoji(Emoji emoji) {
        for (int i = 0; i < Constants.COUNTRIES_COUNT; i++) {
            if (Constants.teamNames.get(i).getEmoji().equals(emoji)) {
                return i;
            }
        }

        return -1;
    }

    private void updateMessage() {
        Button button = Button.of(ButtonStyle.SUCCESS, "start_game",
                Constants.bundle.getString("start_game_button"),
                Emoji.fromFormatted(Constants.bundle.getString("start_emoji_button")));

        getJDA().getTextChannelById(message.getChannelId())
                .editMessageEmbedsById(message.getMessageId(), buildEmbedWithUsers(true))
                .setActionRow(button.asEnabled()).queue();
    }

    private JDA getJDA() { return session.getIoDevice().getJDA(); }
}
