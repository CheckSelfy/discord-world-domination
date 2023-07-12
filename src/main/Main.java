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
import org.javacord.api.entity.emoji.CustomEmoji;
import org.javacord.api.entity.emoji.CustomEmojiBuilder;
import org.javacord.api.entity.emoji.Emoji;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.Reaction;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.user.User;
import org.javacord.api.interaction.SlashCommandBuilder;
import org.javacord.api.interaction.SlashCommandInteraction;

import io.vavr.collection.List.Cons;
import util.Pair;

public class Main {
    public static void main(String[] args) {
        startBot(Constants.properties.getProperty("token"));
    }

    public static void startBot(String token) {
        // Log in the bot
        DiscordApi api = new DiscordApiBuilder().setToken(token).login().join();

        new SlashCommandBuilder().setName("info").setDescription(Constants.bundle.getString("info_description"))
                .createGlobal(api);
        new SlashCommandBuilder().setName("start").setDescription(Constants.bundle.getString("start_description"))
                .createGlobal(api);
            

        api.addSlashCommandCreateListener(event -> {
            SlashCommandInteraction interaction = event.getSlashCommandInteraction();
            if (interaction.getCommandName().equals("info")) {
                interaction.createImmediateResponder().setContent(Constants.bundle.getString("info_message")).respond();
            } else if (interaction.getCommandName().equals("start")) {
                interaction
                        .respondLater()
                        .thenCompose(updater -> updater.setContent(Constants.bundle.getString("start_wait_message")).update())
                        .thenCompose(message -> 
                            message
                                .addReactions(
                                    Constants.EMOJI_TO_COUNTRY.values()
                                            .toArray(new String[Constants.COUNTRIES.size()]))
                                .thenCompose((Void v) -> message.getLatestInstance())
                        )
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

        List<Reaction> reactions = msg.getReactions();

        List<CompletableFuture<Void>> futures = new ArrayList<>(Constants.COUNTRIES.size());
        for (Reaction reaction: reactions) {
            String emoji = reaction.getEmoji().asUnicodeEmoji().get(); // TODO: bad get()
            String country = Constants.EMOJI_TO_COUNTRY.get(emoji); 
            futures.add(reaction.getUsers().thenAcceptAsync(usersSet -> {
                List<String> usersNames = new ArrayList<>();
                for (User user : usersSet)
                    if (!user.isYourself())
                        usersNames.add(user.getMentionTag());
                newList.add(
                    new Pair<>(
                        country + " " + emoji,
                        String.join(", ", usersNames)));
            }));
        }

        EmbedBuilder builder = new EmbedBuilder().setTitle(Constants.bundle.getString("game_name"))
                .setAuthor("Artyom T, @CheckSelf");

        return CompletableFuture
            .allOf(futures.toArray(new CompletableFuture[futures.size()])).thenComposeAsync((Void v) -> {
                for (Pair<String, String> entry: newList) {
                    builder.addField(entry.getFirst(), entry.getSecond());
                }
                return msg.createUpdater().setContent(Constants.bundle.getString("start_get_users")).setEmbed(builder).applyChanges();
            });

        // List<CompletableFuture<Void>> futures = new ArrayList<>(Constants.COUNTRIES.size());
        // for (Map.Entry<String, String> entry : Constants.COUNTRIES.entrySet()) {
        //     String countryName = entry.getKey();
        //     String countryEmoji = entry.getValue();

        //     futures.add(
        //             msg.getReactionByEmoji(Constants.bundle.getString(countryEmoji)).get().getUsers().thenAcceptAsync(
        //                     usersSet -> {
        //                         System.out.println(Constants.bundle.getString(countryName) + " future started");
        //                         List<String> usersNames = new ArrayList<>();
        //                         for (User user : usersSet)
        //                             if (!user.isYourself())
        //                                 usersNames.add(user.getMentionTag());
        //                         newList.add(new Pair<String, String>(
        //                                 Constants.bundle.getString(countryName) + " " + Constants.bundle.getString(countryEmoji),
        //                                 String.join(", ", usersNames)));
        //                         System.out.println(Constants.bundle.getString(countryName) + " future ended");
        //                     }));
        // }

        // return CompletableFuture
        //         .allOf(futures.toArray(new CompletableFuture[futures.size()]));
        //         .thenCompose((Void v) -> {
        //             for (Pair<String, String> entry : newList) {
        //                 builder.addField(entry.getFirst(), entry.getSecond());
        //             }
        //             return msg.createUpdater().setContent(Constants.bundle.getString("start_get_users")).setEmbed(builder)
        //                     .applyChanges();
        //         });
    }
}