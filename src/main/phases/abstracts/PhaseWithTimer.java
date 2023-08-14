package phases.abstracts;

import java.util.Timer;
import java.util.TimerTask;

import net.dv8tion.jda.api.JDA;
import util.Procedure;

public abstract class PhaseWithTimer extends APhase {
    Timer timer;

    public PhaseWithTimer(JDA jda) {
        super(jda);
        timer = new Timer();
    }

    @Override
    public void changeToNextPhase() {
        timer.cancel();
        super.changeToNextPhase();
    }

    abstract public int getDurationInSeconds();

    public boolean isEndless() {
        return getDurationInSeconds() == 0;
    }

    public void schedule(Procedure f) {
        if (isEndless()) {
            return;
        }
        timer.schedule(new Task(f), getDurationInSeconds() * 1000);
    }

    public void scheduleBeforeEnd(Procedure f, int beforeEnd) {
        if (isEndless()) {
            return;
        }
        timer.schedule(new Task(f), (getDurationInSeconds() - beforeEnd) * 1000);
    }

    private static class Task extends TimerTask {
        Procedure f;

        public Task(Procedure f) {
            this.f = f;
        }

        @Override
        public void run() {
            f.execute();
        }
    }

}
