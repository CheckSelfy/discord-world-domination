package util;

import java.util.ArrayList;
import java.util.List;

import languages.TeamLocalization;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.requests.RestAction;

public class GameUtil {
    public static RestAction<List<Void>> putCountriesEmoji(Message msg) {
        List<RestAction<Void>> reactions = new ArrayList<>(Constants.COUNTRIES_COUNT);
        for (TeamLocalization loc : Constants.teamNames) {
            reactions.add(msg.addReaction(loc.getEmoji()));
        }
        return RestAction.allOf(reactions);
    }
}
