import java.util.ArrayList;
import java.util.List;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;

public class App extends ListenerAdapter {
    public static void main(String[] args) throws InterruptedException {
        JDA jda = JDABuilder
                .create(
                        Constants.properties.getProperty("token"),
                        GatewayIntent.getIntents(GatewayIntent.ALL_INTENTS))
                .addEventListeners(new App())
                .build();

        CommandListUpdateAction commands = jda.updateCommands();

        commands.addCommands(
                Commands.slash("info", Constants.bundle.getString("info_description")),
                Commands.slash("start", Constants.bundle.getString("start_description")).setGuildOnly(true));

        commands.queue();
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        switch (event.getName()) {
            case "info":
                event.reply(Constants.bundle.getString("info_message")).queue();
                break;
            case "start":
                event.deferReply().queue();
                Message msg = event.getHook().sendMessage(Constants.bundle.getString("start_wait_message")).complete();
                List<RestAction<Void>> emojisActions = new ArrayList<>(Constants.COUNTRIES_COUNT);
                for (String emoji : Constants.EMOJIS_COUNTRY)
                    emojisActions.add(msg.addReaction(Emoji.fromUnicode(emoji)));

                RestAction.allOf(emojisActions).complete();

                msg.editMessage(Constants.bundle.getString("start_get_users")).queue();

                event.getJDA()
                        .addEventListener(new ListenerStartMessage(msg.getIdLong(), msg.getChannel().getIdLong(), event.getUser().getIdLong()));
                break;
        }
    }
}