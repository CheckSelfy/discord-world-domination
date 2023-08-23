package discord.phases;

import java.util.List;

import discord.DiscordIODevice;
import discord.entities.DiscordMember;
import discord.entities.DiscordTeam;
import discord.entities.DiscordTeamBuilder;
import discord.util.ServerSetupUtil;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import social_logic.Session;
import social_logic.phases.handlers_interfaces.IPresidentPickingPhaseEventHandler;
import social_logic.phases.logic.PresidentPickingPhaseLogic;

public class PresidentPickingPhaseHandler extends ADiscordPhaseEventHandler
        implements IPresidentPickingPhaseEventHandler {
    private final PresidentPickingPhaseLogic<DiscordTeamBuilder, DiscordTeam> logic;

    public PresidentPickingPhaseHandler(Session<DiscordIODevice, IDiscordPhaseEventHandler> session,
            List<DiscordTeamBuilder> builders) {
        super(session);

        this.logic = new PresidentPickingPhaseLogic<>(this, builders);

        ServerSetupUtil util = new ServerSetupUtil(getJDA(), builders);
        util.createChannelsAndRoles(session.getIODevice().getGuildId()).complete();
        util.sendPolls().complete();
        scheduleEnd();
    }

    @Override
    public void onStringSelectInteraction(StringSelectInteractionEvent event) {
        long voter = event.getUser().getIdLong();
        long voted = Long.parseLong(event.getValues().get(0));
        logic.vote(new DiscordMember(voter), new DiscordMember(voted));
        event.deferEdit().queue();
        System.out.println("[" + voter + "] -> " + "[" + voted + "]");
    }

    @Override
    public int getDurationInMilliseconds() { return 1000 * 60 * 3; }

    @Override
    public void phaseEnding() {
        logic.proceedVotes();

        // [DEBUG]
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < logic.getTeamCount(); i++) {
            DiscordTeamBuilder t = logic.getTeamBuilder(i);
            sb.append(t.getDescription().getFullName()).append(": ");
            sb.append(getJDA().getUserById(t.getPresident().getID()).getName()).append("\n");
        }
        getJDA().getTextChannelById(1125882793331785783L).sendMessage(sb.toString()).queue();
        System.out.println("Ended pres-picking.");
    }

    // TODO: remove debug button
    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        event.deferEdit().queue();
        cancelTimer();
        phaseEnding();
    }

    @Override
    public void nextPhase() { session.setPhaseHandler(new TalkingPhaseHandler(session, logic.buildTeams())); }

}
