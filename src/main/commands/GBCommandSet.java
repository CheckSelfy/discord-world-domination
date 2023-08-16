package commands;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import phases.CollectorPhase;
import phases.abstracts.IPhase;
import util.Constants;
import discord.DiscordGuild;
import discord.Session;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.concrete.Category;

public class GBCommandSet extends CommandSet {
    public GBCommandSet() {
        addCommand("info", Constants.bundle.getString("info_description"), GBCommandSet::infoCommand);

        addCommand(Commands.slash("clear", "Clear categories (and vc'es inside) + ALL ROLES! [DEBUG]")
                .setDefaultPermissions(
                        DefaultMemberPermissions.enabledFor(Permission.MANAGE_CHANNEL, Permission.MODERATE_MEMBERS))
                .setGuildOnly(true), GBCommandSet::clearCommand);

        addCommand(Commands.slash("start", Constants.bundle.getString("start_description")).setGuildOnly(true),
                GBCommandSet::startCommand);
    }

    private static void infoCommand(SlashCommandInteractionEvent event) {
        event.reply(Constants.bundle.getString("info_message")).queue();
    }

    private static void clearCommand(SlashCommandInteractionEvent event) {
        event.deferReply().complete();
        for (Category category : event.getGuild().getCategories()) {
            System.out.print(category.getName() + " ?= " + Constants.bundle.getString("game_name"));
            if (category.getName().equals(Constants.bundle.getString("game_name"))) {
                System.out.println(" yes");
                for (Channel ch : category.getChannels()) {
                    ch.delete().complete();
                }
                category.delete().complete();
            } else {
                System.out.println(" no");
            }
        }

        try {
            for (Role role : event.getGuild().getRoles()) {
                if (role.compareTo(role.getGuild().getSelfMember().getRoles().get(0)) < 0) {
                    System.out.println("role deleted");
                    role.delete().complete();
                }
            }
        } catch (Exception e) {
            ; // TODO ??!?!?!?!?
        }

        for (Object obj : event.getJDA().getRegisteredListeners()) {
            if (obj instanceof IPhase)
                event.getJDA().removeEventListener(obj);
        }

        event.getHook().sendMessage("Done :)").queue();
    }

    private static void startCommand(SlashCommandInteractionEvent event) {
        // new session starts here
        event.getJDA().addEventListener(
                new Session(event.getJDA(), new DiscordGuild(event.getGuild().getIdLong())));
    }
}
