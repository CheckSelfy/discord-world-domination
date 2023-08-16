package discord.phases.handlers;

import discord.Session;
import discord.phases.CollectorPhaseLogic;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.react.GenericMessageReactionEvent;

public class CollectorPhaseHandler extends APhaseEventHandler {
    // MessageWithPrivilegeUserChecker message;
    // ArrayList<Set<Long>> teams;

    public CollectorPhaseHandler(Session session) { super(session, new CollectorPhaseLogic()); }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        // if (!event.getButton().getId().equals("start_game") || // not start button
        // !message.check(event.getMessage())) {
        // return;
        // }

        // if (!message.checkUser(event.getUser())) {
        // event.reply(Constants.bundle.getString("start_not_creator")).setEphemeral(true).queue();
        // return;
        // }

        // // blocking the button
        // event.deferEdit().queue();
        // MessageEditData changes = new
        // MessageEditBuilder().setComponents().setEmbeds(buildEmbedWithUsers(false))
        // .build();

        // event.getHook().editMessageById(message.getMessageId(), changes) // applying
        // changes
        // .flatMap(msg -> msg.clearReactions()).queue();

        // nextPhase();
    }

    // private MessageEmbed buildEmbedWithUsers(boolean addEmptyFields) {
    // EmbedBuilder builder = Constants.getEmptyEmbedBuilder();

    // for (int i = 0; i < Constants.COUNTRIES_COUNT; i++) {
    // StringJoiner joiner = new StringJoiner(", ");
    // for (Long user : teams.get(i))
    // joiner.add(DiscordUtil.getDiscordMentionTag(user));

    // if (addEmptyFields || joiner.length() != 0)
    // builder.addField(Constants.teamNames.get(i).getFullName(), joiner.toString(),
    // false);
    // }

    // return builder.build();
    // }

    // private void updateMessage() {
    // Button button = Button.of(ButtonStyle.SUCCESS, "start_game",
    // Constants.bundle.getString("start_game_button"),
    // Emoji.fromFormatted(Constants.bundle.getString("start_emoji_button")));

    // session.getJDA().getTextChannelById(message.getChannelId())
    // .editMessageEmbedsById(message.getMessageId(), buildEmbedWithUsers(true))
    // .setActionRow(button.asEnabled()).queue();
    // }

    //
    //
    //

    @Override
    public void onGenericMessageReaction(GenericMessageReactionEvent event) {
        // if (!message.check(event)) {
        // return;
        // }

        // int team = getTeamByEmoji(event.getEmoji());
        // if (team == -1)
        // return;

        // if (event instanceof MessageReactionAddEvent)
        // teams.get(team).add(event.getUserIdLong());
        // else
        // teams.get(team).remove(event.getUserIdLong());

        // updateMessage();
    }

    // public static int getTeamByEmoji(Emoji emoji) {
    // for (int i = 0; i < Constants.COUNTRIES_COUNT; i++) {
    // if (Constants.teamNames.get(i).getEmoji().equals(emoji)) {
    // return i;
    // }
    // }

    // return -1;
    // }

    //
    //
    //

    public void nextPhase() { session.setPhase(new PresidentPickingPhaseHandler(session)); }

    /* some method */ {
        // decided to change to next phase
        nextPhase();
    }

}
