package phases.abstracts;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public abstract class APhase extends ListenerAdapter implements IPhase {
    private JDA jda;

    public APhase(JDA jda) { this.jda = jda; }

    public void changeToNextPhase() {
        jda.removeEventListener(this);
        jda.addEventListener(nextPhase());
    }

    @Override
    public JDA getJDA() { return jda; }
}
