import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;

import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.component.ActionRow;
import org.javacord.api.entity.message.component.Button;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.user.User;
import org.javacord.api.interaction.ButtonInteraction;
import org.javacord.api.interaction.SlashCommandBuilder;
import org.javacord.api.interaction.SlashCommandInteraction;
import org.javacord.api.interaction.callback.InteractionOriginalResponseUpdater;

import util.Pair;

public class Main {
    static Locale locale;
    static ResourceBundle bundle;
    static Properties properties;

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.print("Usage: java <name-of-executable> <lang>");
            return;
        }

        try {
            bundle = ResourceBundle.getBundle("languages/lang", new Locale.Builder().setLanguage(args[0]).build());
        } catch (Throwable e) {
            e.printStackTrace();
            return;
        }

        properties = new Properties();
        try (FileInputStream fin = new FileInputStream("settings.properties")) {
            properties.load(fin);
            startBot(properties.getProperty("token"));
        } catch (Throwable e) {
            System.err.println("Error with settings.properties");
            e.printStackTrace();
            return;
        }
    }

    public static void startBot(String token) {
        // Log in the bot
        DiscordApi api = new DiscordApiBuilder().setToken(token).login().join();

        new SlashCommandBuilder().setName("info").setDescription(bundle.getString("info_description"))
                .createGlobal(api);
        new SlashCommandBuilder().setName("start").setDescription(bundle.getString("start_description"))
                .createGlobal(api);

        api.addSlashCommandCreateListener(event -> {
            SlashCommandInteraction interaction = event.getSlashCommandInteraction();
            if (interaction.getCommandName().equals("info")) {
                interaction.createImmediateResponder().setContent(bundle.getString("info_message")).respond();
            } else if (interaction.getCommandName().equals("start")) {
                interaction
                        .respondLater()
                        .thenCompose(updater -> updater.setContent(bundle.getString("start_wait_message")).update())
                        .thenCompose(message -> {
                            List<String> emojis = new ArrayList<>(Constants.COUNTRIES.size());
                            for (String emojiName : Constants.COUNTRIES.values())
                                emojis.add(bundle.getString(emojiName));
                            return message.addReactions(emojis.toArray(new String[emojis.size()]))
                                    .thenCompose((Void v) -> message.getLatestInstance()); // TODO: bad reforwarding
                                                                                           // message имхо
                        })
                        .thenCompose(message -> updatePlayersInfo(message))
                        .thenAccept(message -> {
                            message.addReactionAddListener(reactionHappeded -> updatePlayersInfo(message)); // TODO:
                                                                                                            // double
                                                                                                            // listeners
                            message.addReactionRemoveListener(reactionHappeded -> updatePlayersInfo(message));
                        }).join();
                ;

            }
        });

    }

    static public CompletableFuture<Message> updatePlayersInfo(Message msg) { // too expensive
        List<Pair<String, String>> newList = Collections.synchronizedList(new ArrayList<>(Constants.COUNTRIES.size()));

        List<CompletableFuture<Void>> futures = new ArrayList<>(Constants.COUNTRIES.size());
        for (Map.Entry<String, String> entry : Constants.COUNTRIES.entrySet()) {
            String countryName = entry.getKey();
            String countryEmoji = entry.getValue();

            futures.add(
                    msg.getReactionByEmoji(bundle.getString(countryEmoji)).get().getUsers().thenAccept(
                            usersSet -> {
                                System.out.println(bundle.getString(countryName) + " future started");
                                List<String> usersNames = new ArrayList<>();
                                for (User user : usersSet)
                                    if (!user.isYourself())
                                        usersNames.add(user.getMentionTag());
                                newList.add(new Pair<String, String>(
                                        bundle.getString(countryName) + " " + bundle.getString(countryEmoji),
                                        String.join(", ", usersNames)));
                                System.out.println(bundle.getString(countryName) + " future ended");
                            }));
        }

        EmbedBuilder builder = new EmbedBuilder().setTitle(bundle.getString("game_name"))
                .setAuthor("Artyom T, @CheckSelf");

        return CompletableFuture
                .allOf(futures.toArray(new CompletableFuture[futures.size()]))
                .thenCompose((Void v) -> {
                    for (Pair<String, String> entry : newList) {
                        builder.addField(entry.getFirst(), entry.getSecond());
                    }
                    return msg.createUpdater().setContent(bundle.getString("start_get_users")).setEmbed(builder)
                            .applyChanges();
                });
    }
}