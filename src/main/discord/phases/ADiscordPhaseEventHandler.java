package discord.phases;

import java.util.Timer;
import java.util.TimerTask;

import discord.DiscordIODevice;
import net.dv8tion.jda.api.JDA;
import social_logic.Session;
import util.Procedure;

public abstract class ADiscordPhaseEventHandler implements IDiscordPhaseEventHandler {
    protected final Session<DiscordIODevice, IDiscordPhaseEventHandler> session;
    private final Timer timer;

    public ADiscordPhaseEventHandler(Session<DiscordIODevice, IDiscordPhaseEventHandler> session) {
        this.session = session;
        this.timer = new Timer();
    }

    protected void scheduleEnd() {
        schedule(this::phaseEnding, getDurationInMilliseconds());
    }

    private void schedule(Procedure f, int timeInMilliSeconds) {
        timer.schedule(new TimerTask() {
            @Override
            public void run() { f.execute(); }
        }, timeInMilliSeconds);
    }

    protected void scheduleBeforeEnd(Procedure f, int timeInMilliSeconds) {
        schedule(f, getDurationInMilliseconds() - timeInMilliSeconds);
    }

    protected void cancelTimer() {
        timer.cancel();
    }


    protected JDA getJDA() { return session.getIODevice().getJDA(); }

}
