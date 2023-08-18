package app;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;

import commands.CommandSet;
import commands.GBCommandSet;
import discord.DiscordIODevice;
import discord.phases.IDiscordPhaseEventHandler;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import social_logic.Session;
import util.Constants;

public class App extends ListenerAdapter {
    private static CommandSet commands;
    private static ArrayList<Session<DiscordIODevice, IDiscordPhaseEventHandler>> session;

    public static void main(String[] args) throws InterruptedException, IOException {
        JDABuilder.create(loadToken(), EnumSet.allOf(GatewayIntent.class)).addEventListeners(new App()).build();

        commands = new GBCommandSet();
    }

    private static String loadToken() throws IOException {
        try (FileInputStream fin = new FileInputStream("token.txt")) {
            return new String(fin.readAllBytes());
        } catch (FileNotFoundException e) {
            throw new FileNotFoundException(Constants.bundle.getString("token_absent"));
        } catch (IOException e) {
            throw new IOException(Constants.bundle.getString("token_ioexception"), e);
        }
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) { commands.accept(event); }

    // Session lifetime control
    public static void addNewSession(final Session<DiscordIODevice, IDiscordPhaseEventHandler> s) { session.add(s); }

    public static void removeSession(final Session<DiscordIODevice, IDiscordPhaseEventHandler> s) { session.remove(s); }

}
