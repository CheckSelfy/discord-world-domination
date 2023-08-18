package commands;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
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

        addCommand(Commands.slash("start", Constants.bundle.getString("start_description")).setGuildOnly(true),
                GBCommandSet::startCommand);
    }

    private static void infoCommand(SlashCommandInteractionEvent event) {
        event.reply(Constants.bundle.getString("info_message")).queue();
    }

    private static void startCommand(SlashCommandInteractionEvent event) {
        DiscordIODevice ioDevice = new DiscordIODevice(event.getJDA(), event.getGuild().getIdLong());
        Session<DiscordIODevice, IDiscordPhaseEventHandler> session = new Session<>(ioDevice);
        session.setPhase(
                new CollectorPhaseHandler(session, event.getChannel().getIdLong(), event.getUser().getIdLong()));
        ioDevice.setSession(session);
        App.addNewSession(session);
        event.getJDA().addEventListener(ioDevice);

    }

}
