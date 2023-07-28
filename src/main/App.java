import java.util.EnumSet;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;

public class App extends ListenerAdapter {
    public static void main(String[] args) throws InterruptedException {
        JDA jda = JDABuilder
                .create(
                        Constants.properties.getProperty("token"),
                        EnumSet.allOf(GatewayIntent.class))
                .addEventListeners(new App())
                .build();

        CommandListUpdateAction commands = jda.updateCommands();

        commands.addCommands(
                Commands.slash("info", Constants.bundle.getString("info_description")),
                Commands.slash("clear", "Clear categories (and vc'es inside) + ALL ROLES! [DEBUG]")
                        .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MANAGE_CHANNEL, Permission.MODERATE_MEMBERS)),
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

                Message msg = event.getHook().sendMessageEmbeds(
                        new EmbedBuilder()
                                .setTitle(Constants.bundle.getString("game_name"))
                                .setDescription("Click your emojis!").build())
                        .complete();

                Team.putCountriesEmoji(msg).complete();

                event.getJDA()
                        .addEventListener(
                                new ListenerStartMessage(
                                        msg.getIdLong(),
                                        msg.getChannel().getIdLong(),
                                        event.getUser().getIdLong()));
                break;
           case "clear":
                event.deferReply().complete();
                for (Category category: event.getGuild().getCategories()) {
                        if (category.getName().equals("Global domination")) {
                                for (Channel ch: category.getChannels())
                                        ch.delete().queue();
                                category.delete().queue();
                        }
                }

                // for (Role role: event.getGuild().getRoles()) {
                //         if (role.getName().equals("global-domination"))
                //                 return;
                //         System.out.println(role);
                //         role.delete().queue();
                // }

                event.getHook().sendMessage("All cleared.").queue();
                break;
        }
    }
}