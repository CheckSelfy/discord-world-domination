package commands;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import app.App;
import discord.DiscordIODevice;
import discord.entities.DiscordMember;
import discord.entities.DiscordTeam;
import discord.entities.DiscordTeamBuilder;
import discord.phases.CollectorPhaseHandler;
import discord.phases.IDiscordPhaseEventHandler;
import discord.phases.TalkingPhaseHandler;
import discord.util.ServerSetupUtil;
import game.Game;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import social_logic.Session;
import util.Constants;

public class GBCommandSet extends CommandSet {
    public GBCommandSet() {
        addCommand("info", Constants.bundle.getString("info_description"), GBCommandSet::infoCommand);

        addCommand(Commands.slash("clear", "Clear categories (and vc'es inside) + ALL ROLES! [DEBUG]")
                .setDefaultPermissions(
                        DefaultMemberPermissions.enabledFor(Permission.MANAGE_CHANNEL, Permission.MODERATE_MEMBERS))
                .setGuildOnly(true), GBCommandSet::clearCommand);

        addCommand(Commands.slash("start", Constants.bundle.getString("start_description")).setGuildOnly(true),
                GBCommandSet::startCommand);
        addCommand("shortcut", "shortcut", GBCommandSet::skipCollectorAndPickingPhase);
    }

    private static void infoCommand(SlashCommandInteractionEvent event) {
        event.reply(Constants.bundle.getString("info_message")).queue();
    }

    private static void startCommand(SlashCommandInteractionEvent event) {
        if (App.haveRunningSessionInGuild(event.getGuild().getIdLong())) {
            event.reply("Game have been already created!").setEphemeral(true).queue();
            return;
        }

        Session<DiscordIODevice, IDiscordPhaseEventHandler> session = createSession(event.getJDA(), event.getGuild());
        session.setPhaseHandler(
                new CollectorPhaseHandler(session, event.getChannel().getIdLong(), event.getUser().getIdLong()));

        event.reply("Game created!").queue();

    }

    private static Session<DiscordIODevice, IDiscordPhaseEventHandler> createSession(JDA jda, Guild guild) {
        DiscordIODevice ioDevice = new DiscordIODevice(jda, guild.getIdLong());
        jda.addEventListener(ioDevice);
        Session<DiscordIODevice, IDiscordPhaseEventHandler> session = new Session<>(ioDevice);
        ioDevice.setSession(session);
        App.addNewSession(session);
        return session;
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

        App.removeSession(event.getGuild().getIdLong());
        event.getHook().sendMessage("Done :)").queue();
    }

    //
    //
    //

    private static final long ismaxisID = 292778788809474050L;
    private static final long maksobotID = 1144015810453586072L;
    private static final long checkselfID = 219019680013221888L;
    private static final long checkselfyID = 1120024604673577113L;
    private static final DiscordMember ismaxis = new DiscordMember(ismaxisID);
    private static final DiscordMember maksobot = new DiscordMember(maksobotID);
    private static final DiscordMember checkself = new DiscordMember(checkselfID);
    private static final DiscordMember checkselfy = new DiscordMember(checkselfyID);

    private static final List<DiscordTeamBuilder> builders = List.of(
            new DiscordTeamBuilder().setMembers(Set.of(ismaxis, checkselfy)).setPresident(ismaxis)
                    .setDescription(Constants.teamNames.get(0)),
            new DiscordTeamBuilder().setMembers(Set.of(checkself, maksobot)).setPresident(maksobot)
                    .setDescription(Constants.teamNames.get(1)));

    private static void skipCollectorAndPickingPhase(SlashCommandInteractionEvent event) {
        event.deferReply().complete();

        JDA jda = event.getJDA();

        ServerSetupUtil util = new ServerSetupUtil(jda, builders);
        util.createChannelsAndRoles(event.getGuild().getIdLong()).complete();

        Session<DiscordIODevice, IDiscordPhaseEventHandler> session = createSession(event.getJDA(), event.getGuild());
        session.setPhaseHandler(new TalkingPhaseHandler(session, new Game<DiscordTeam>(buildTeams(builders))));

        event.getHook().sendMessage("Collector and Picking phases skipped")
                .setEphemeral(true).queue();

    }

    private static List<DiscordTeam> buildTeams(List<DiscordTeamBuilder> builders) {
        List<DiscordTeam> teams = new ArrayList<>(builders.size());
        for (DiscordTeamBuilder builder : builders) {
            teams.add(builder.build());
        }
        return teams;
    }

}
