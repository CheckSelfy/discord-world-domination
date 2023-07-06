import java.awt.Color;
import java.util.ArrayList;
import java.util.Set;

import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.channel.ChannelCategory;
import org.javacord.api.entity.channel.ChannelCategoryBuilder;
import org.javacord.api.entity.channel.ServerVoiceChannel;
import org.javacord.api.entity.channel.ServerVoiceChannelBuilder;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.channel.VoiceChannel;
import org.javacord.api.entity.intent.Intent;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.component.Button;
import org.javacord.api.entity.permission.RoleBuilder;
import org.javacord.api.entity.server.Server;
import org.javacord.api.event.Event;
import org.javacord.api.interaction.SlashCommand;

import kotlin.collections.builders.SetBuilder;

public class Main {
        static String token = "MTEyNTg4MjIxNDc0NDM0MjcwOA.GtAE-i.cDNZRioZWCtjY9kSdPFMN3rDUw6DFf1nU02Ik0";

        static Set<String> countries = Set.of("Russia 🇷🇺", "USA 🇺🇸", "North Korea 🇰🇵", "China 🇨🇳", "Iran 🇮🇷", "Germany 🇩🇪");

        public static void main(String[] args) {
                // Log the bot in
                DiscordApi api = new DiscordApiBuilder()
                                .setToken(token)
                                .addIntents(Intent.MESSAGE_CONTENT)
                                .login().join();

                // Get link to invite bot
                System.out.println(api.createBotInvite(org.javacord.api.entity.permission.Permissions.fromBitmask(8)));


                SlashCommand command = SlashCommand.with("start", "Creates a game")
                                .createGlobal(api)
                                .join();


                api.addSlashCommandCreateListener(event -> {
                        if (event.getSlashCommandInteraction().getCommandName().equals("start")) {
                                System.out.println("start logged.");

                                Server server = event.getInteraction().getServer().get();

                                new RoleBuilder(server).setName("Russia RU").setColor(Color.BLUE).create();

                                ChannelCategory category = new ChannelCategoryBuilder(server).setName("Global Domination Game").create().join();

                                for (String str: countries) {
                                        new ServerVoiceChannelBuilder(server).setCategory(category).setName(str).create();
                                }

                                api.wait(0, 0);

                                System.out.println("Created chs:");
                                category.getChannels().forEach(ch -> {System.out.println(ch);});

                                event.getSlashCommandInteraction().createImmediateResponder().setContent("Game started.").respond();
                                event.getSlashCommandInteraction().getChannel().ifPresent(channel -> {
                                        new MessageBuilder().addActionRow(Button.danger("deleteChannels", "delete channels")).send(channel).join().addButtonClickListener(
                                                buttonEvent -> {
                                                        if (buttonEvent.getButtonInteraction().getCustomId().equals("deleteChannels")) {
                                                                for (long id: created_voices) {
                                                                        server.getVoiceChannelById(id).ifPresent(vc -> {
                                                                                vc.delete("End of game.");
                                                                        });
                                                                }

                                                                category.delete("End of game.");
                                                                buttonEvent.getButtonInteraction().createImmediateResponder().setContent("GG WP! :D").respond();
                                                        }
                                                }
                                        );
                                });
                        }
                });

        }
}