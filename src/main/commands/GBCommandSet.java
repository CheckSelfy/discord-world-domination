package commands;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import social_logic.Session;
import util.Constants;
import discord.DiscordIODevice;
import discord.phases.CollectorPhaseHandler;
import discord.phases.IDiscordPhaseEventHandler;

import app.App;

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

    private static void startCommand(SlashCommandInteractionEvent event) {
        if (App.haveRunningSessionInGuild(event.getGuild().getIdLong())) {
            event.reply("Game have been already created!").setEphemeral(true).queue();
            return;
        }

        DiscordIODevice ioDevice = new DiscordIODevice(event.getJDA(), event.getGuild().getIdLong());
        Session<DiscordIODevice, IDiscordPhaseEventHandler> session = new Session<>(ioDevice);
        event.reply("Game created!").queue();
        session.setPhase(
                new CollectorPhaseHandler(session, event.getChannel().getIdLong(), event.getUser().getIdLong()));
        ioDevice.setSession(session);
        App.addNewSession(session);
        event.getJDA().addEventListener(ioDevice);

    }

    private static void clearCommand(SlashCommandInteractionEvent event) {
        event.deferReply().complete();
        for (Category category : event.getGuild().getCategories()) {
            System.out.print(category.getName() + " ?= " + Constants.bundle.getString("game_name"));
            if (category.getName().equals(Constants.bundle.getString("game_name"))) {
                for (Channel ch : category.getChannels()) {
                    ch.delete().complete();
                }
                category.delete().complete();
            }
        }

        try {
            for (Role role : event.getGuild().getRoles()) {
                if (role.compareTo(role.getGuild().getSelfMember().getRoles().get(0)) < 0) {
                    role.delete().complete();
                }
            }
        } catch (Exception e) {
            System.err.println(e);
            System.err.println("Exception ignored");
        }

        for (Object obj : event.getJDA().getRegisteredListeners()) {
            if (obj instanceof DiscordIODevice device) {
                if (device.getGuildId() == event.getGuild().getIdLong()) {
                    event.getJDA().removeEventListener(obj);
                    break;
                }
            }
        }

        event.getHook().sendMessage("Done :)").queue();
    }

}
