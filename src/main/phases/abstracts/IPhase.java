package phases.abstracts;

import net.dv8tion.jda.api.JDA;

public interface IPhase {
    IPhase nextPhase(JDA jda);
}
