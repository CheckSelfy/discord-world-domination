import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.emoji.Emoji;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.Reaction;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.user.User;
import org.javacord.api.interaction.SlashCommandBuilder;
import org.javacord.api.interaction.SlashCommandInteraction;
import org.javacord.core.entity.emoji.UnicodeEmojiImpl;

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
        new SlashCommandBuilder().setName("test").setDescription("TEST")
                .createGlobal(api);
            

        api.addSlashCommandCreateListener(event -> {
            SlashCommandInteraction interaction = event.getSlashCommandInteraction();
            if (interaction.getCommandName().equals("info")) {
                // interaction.createImmediateResponder().setContent(Constants.bundle.getString("info_message")).respond();
            } else if (interaction.getCommandName().equals("start")) {
                String[] emojis = Constants.EMOJI_TO_COUNTRY.keySet().toArray(new String[Constants.COUNTRIES.size()]);
                interaction.respondLater();
                
                // api.getMessageById(12, api.getTextChannelById(34)).addEmoji

                interaction
                    .createFollowupMessageBuilder()
                    .setContent(Constants.bundle.getString("start_wait_message"))
                    .send()
                    .thenApplyAsync(message -> message);
                        
            } else if (interaction.getCommandName().equals("test")) {

            }
        });

    }

    static public CompletableFuture<Message> updatePlayersInfo(Message msg) { // too expensive
        List<Pair<String, String>> newList = Collections.synchronizedList(new ArrayList<>(Constants.COUNTRIES.size()));

        List<Reaction> reactions = msg.getReactions();

        EmbedBuilder builder = new EmbedBuilder().setTitle(Constants.bundle.getString("game_name"))
                .setAuthor("Artyom T, @CheckSelf");

        List<CompletableFuture<Void>> futures = new ArrayList<>(Constants.COUNTRIES.size());
        for (Reaction reaction: reactions) {
            String emoji = reaction.getEmoji().asUnicodeEmoji().get(); // TODO: bad get()
            String country = Constants.EMOJI_TO_COUNTRY.get(emoji); 
            futures.add(reaction.getUsers().thenAcceptAsync(usersSet -> {
                List<String> usersNames = new ArrayList<>();
                for (User user : usersSet)
                    if (!user.isYourself())
                        usersNames.add(user.getMentionTag());
                // newList.add(
                //     new Pair<>(
                //         country + " " + emoji,
                //         String.join(", ", usersNames)));
                builder.addField(country + " " + emoji, String.join(", ", usersNames)); // TODO: is it thread-safe?
            }));
        }


        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[futures.size()])).thenComposeAsync(
            (Void v) -> msg.createUpdater().setContent(Constants.bundle.getString("start_get_users")).setEmbed(builder).applyChanges()
        );
        // return CompletableFuture
        //     .allOf(futures.toArray(new CompletableFuture[futures.size()])).thenComposeAsync((Void v) -> {
        //         for (Pair<String, String> entry: newList) {
        //             builder.addField(entry.getFirst(), entry.getSecond());
        //         }
        //         return msg.createUpdater().setContent(Constants.bundle.getString("start_get_users")).setEmbed(builder).applyChanges();
        //     });
    }
}