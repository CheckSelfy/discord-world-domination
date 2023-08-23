package discord.phases;

import java.util.ArrayList;
import java.util.Collections;

import discord.DiscordIODevice;
import discord.entities.DiscordMember;
import discord.entities.DiscordTeamProperty;
import discord.util.ServerSetupUtil;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import social_logic.Session;
import social_logic.entities.TeamBuilder;
import social_logic.phases.handlers_interfaces.IPresidentPickingPhaseEventHandler;
import social_logic.phases.logic.PresidentPickingPhaseLogic;

public class PresidentPickingPhaseHandler extends ADiscordPhaseEventHandler
        implements IPresidentPickingPhaseEventHandler {
    private final PresidentPickingPhaseLogic phaseLogic;
    private final ArrayList<DiscordTeamProperty> properties;

    public PresidentPickingPhaseHandler(Session<DiscordIODevice, IDiscordPhaseEventHandler> session,
            ArrayList<TeamBuilder> builders) {
        super(session);

        this.phaseLogic = new PresidentPickingPhaseLogic(this, builders);
        this.properties = new ArrayList<>(Collections.nCopies(builders.size(), null));

        ServerSetupUtil util = new ServerSetupUtil(getJDA(), builders, properties);
        util.createChannelsAndRoles(session.getIODevice().getGuildId()).complete();
        util.sendPolls().complete();
        scheduleEnd();
    }

    @Override
    public void onStringSelectInteraction(StringSelectInteractionEvent event) {
        long voter = event.getUser().getIdLong();
        long voted = Long.parseLong(event.getValues().get(0));
        phaseLogic.vote(new DiscordMember(voter), new DiscordMember(voted));
        event.deferEdit().queue();
        System.out.println("[" + voter + "] -> " + "[" + voted + "]");
    }

    @Override
    public int getDurationInMilliseconds() { return 1000 * 60 * 3; }

    @Override
    public void phaseEnding() {
        phaseLogic.proceedVotes();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < phaseLogic.getTeamCount(); i++) {
            TeamBuilder t = phaseLogic.getTeamBuilder(i);
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

    public void nextPhase() {
        System.out.println("Next phase");
        /* session.setPhase(new TalkingPhase(session)); */
    }

}
