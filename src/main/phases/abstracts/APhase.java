package phases.abstracts;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public abstract class APhase extends ListenerAdapter implements IPhase {
    public void changeToNextPhase(JDA jda) {
        jda.removeEventListener(this);
        jda.addEventListener(nextPhase(jda));
    }
}
