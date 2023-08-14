package util;

import java.util.ArrayList;
import java.util.List;

import game.entities.Team;
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

    public static RestAction<?> putCountriesEmoji(Message msg, List<? extends Team> teams, Team exceptFor) {
        List<RestAction<Void>> reactions = new ArrayList<>(Constants.COUNTRIES_COUNT);
        for (Team team: teams) {
            if (team.equals(exceptFor)) 
                continue;
            reactions.add(msg.addReaction(team.getLocalization().getEmoji()));
        }
        return RestAction.allOf(reactions);
    }

    public static RestAction<?> putCountriesEmoji(Message msg, ArrayList<? extends Team> teams, int exceptForIndex) {
        List<RestAction<Void>> reactions = new ArrayList<>(Constants.COUNTRIES_COUNT);
        for (int i = 0; i < teams.size(); i++) {
            if (i == exceptForIndex) 
                continue;
            reactions.add(msg.addReaction(teams.get(i).getLocalization().getEmoji()));
        }
        return RestAction.allOf(reactions);
    }
}
