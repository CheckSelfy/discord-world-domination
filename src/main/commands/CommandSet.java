package commands;

import java.util.ArrayList;
import java.util.function.Consumer;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public class CommandSet {
    private ArrayList<SlashCommandData> commands;
    private ArrayList<Consumer<SlashCommandInteractionEvent>> consumers;

    public CommandSet() {
        commands = new ArrayList<>();
        consumers = new ArrayList<>();
    }

    public void clear() {
        commands = new ArrayList<>();
        consumers = new ArrayList<>();
    }

    public void addCommand(String name, String description, Consumer<SlashCommandInteractionEvent> consumer) {
        addCommand(Commands.slash(name, description), consumer);
    }

    public void addCommand(SlashCommandData command, Consumer<SlashCommandInteractionEvent> consumer) {
        commands.add(command);
        consumers.add(consumer);
    }

    private int getCommandDataIndex(String commandId) {
        for (int i = 0; i < commands.size(); i++) {
            if (commands.get(i).getName().equals(commandId)) {
                return i;
            }
        }
        return -1;
    }

    public boolean check(String commandId) { return getCommandDataIndex(commandId) != -1; }

    public void accept(SlashCommandInteractionEvent event) {
        int index = getCommandDataIndex(event.getName());
        if (index != -1) {
            consumers.get(index).accept(event);
        }
    }

    public void updateCommands(JDA jda) { jda.updateCommands().addCommands(commands).queue(); }
}
