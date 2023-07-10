import java.io.FileInputStream;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;

import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.message.component.ActionRow;
import org.javacord.api.entity.message.component.Button;
import org.javacord.api.interaction.SlashCommandBuilder;
import org.javacord.api.interaction.SlashCommandInteraction;

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
                interaction.respondLater().thenAccept(startCommandInteraction -> {
                    startCommandInteraction.setContent(bundle.getString("start_wait_message")).update();

                    interaction.createFollowupMessageBuilder()
                        .setContent(bundle.getString("start_get_users"))
                        .addComponents(ActionRow.of(Button.primary("start_game_button", bundle.getString("start_game_button"))))
                        .send()
                        .thenAccept(
                            msg -> {
                                msg.addReaction(bundle.getString("ru_emoji"));
                                msg.addReaction(bundle.getString("us_emoji"));
                                msg.addReaction(bundle.getString("de_emoji"));
                                msg.addReaction(bundle.getString("cn_emoji"));
                                msg.addReaction(bundle.getString("ir_emoji"));
                                msg.addReaction(bundle.getString("kp_emoji"));

                                msg.addButtonClickListener(buttonClick -> {
                                    buttonClick
                                });
                            }
                        );
                });
            }
        });
    }
}