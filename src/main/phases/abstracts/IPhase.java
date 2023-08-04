package phases.abstracts;

import net.dv8tion.jda.api.JDA;

public interface IPhase {
    public IPhase nextPhase();

    public JDA getJDA();
}