package discord.phases;

import java.util.List;

import discord.DiscordIODevice;
import social_logic.Session;
import social_logic.entities.Team;
import social_logic.phases.handlers_interfaces.ITalkingPhaseEventHandler;

public class TalkingPhaseEventHandler extends ADiscordPhaseEventHandler
        implements ITalkingPhaseEventHandler {

    public TalkingPhaseEventHandler(Session<DiscordIODevice, IDiscordPhaseEventHandler> session, List<Team> teams) {
        super(session);
    }

    @Override
    public void phaseEnding() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'phaseEnding'");
    }

    @Override
    public int getDurationInMilliseconds() { return 0; }

}
