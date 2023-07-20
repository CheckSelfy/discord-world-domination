import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.requests.RestAction;

public class Team {
    private Set<Long> users; // members of country
    private Long roleId;
    private Long voiceChannelId;
    private TeamLocalization localization;

    private Long sendDelegationMessageId;
    private Long receiveDelegationMessageId;

    private Set<Team> receivedRequests;

    private Long actionMesageId;

    public Team(TeamLocalization localization) {
        this.localization = localization;
        users = new HashSet<>();
        roleId = null;
        voiceChannelId = null;
        sendDelegationMessageId = null;
        receiveDelegationMessageId = null;
        receivedRequests = new HashSet<>();
        actionMesageId = null;
    }

    public static Team getTeamByEmoji(final List<Team> teams, Emoji emoji) {
        for (Team team : teams)
            if (team.localization.getEmoji().equals(emoji))
                return team;
        return null;
    }

    public static Team getTeamBySendDelegationMessage(final List<Team> teams, long channelId, long msgId) {
        for (Team team : teams)
            if (team.getSendDelegationMessageId().equals(msgId) &&
                    team.getVoiceChannelId().equals(channelId))
                return team;
        return null;
    }

    public static Team getTeamByReceiveDelegationMessage(final List<Team> teams, long channelId, long msgId) {
        for (Team team : teams)
            if (team.getReceiveDelegationMessageId().equals(msgId) &&
                    team.getVoiceChannelId().equals(channelId))
                return team;
        return null;
    }

    public static Team getTeamByName(final List<Team> teams, String name) {
        for (Team team : teams)
            if (team.getLocalization().getName().equals(name))
                return team;
        return null;
    }

    public static RestAction<List<Void>> putCountriesEmoji(Message msg) {
        List<RestAction<Void>> emojisActions = new ArrayList<>(Constants.COUNTRIES_COUNT);
        for (TeamLocalization localization : Constants.teamNames)
            emojisActions.add(msg.addReaction(localization.getEmoji()));

        return RestAction.allOf(emojisActions);
    }

    public static RestAction<List<Void>> putCountriesEmoji(Message msg, List<Team> teams, Team exceptTeam) {
        List<RestAction<Void>> emojisActions = new ArrayList<>(Constants.COUNTRIES_COUNT);
        for (Team team : teams)
            if (!team.equals(exceptTeam))
                emojisActions.add(msg.addReaction(team.getLocalization().getEmoji()));

        return RestAction.allOf(emojisActions);
    }

    public Set<Long> getUsers() {
        return users;
    }

    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }

    public Long getVoiceChannelId() {
        return voiceChannelId;
    }

    public void setVoiceChannelId(Long voiceChannelId) {
        this.voiceChannelId = voiceChannelId;
    }

    public TeamLocalization getLocalization() {
        return localization;
    }

    public Long getSendDelegationMessageId() {
        return sendDelegationMessageId;
    }

    public void setSendDelegationMessageId(Long sendDelegationMessageId) {
        this.sendDelegationMessageId = sendDelegationMessageId;
    }

    public Long getReceiveDelegationMessageId() {
        return receiveDelegationMessageId;
    }

    public void setReceiveDelegationMessageId(Long receiveDelegationMessageId) {
        this.receiveDelegationMessageId = receiveDelegationMessageId;
    }

    public Long getActionMesageId() {
        return actionMesageId;
    }

    public void setActionMesageId(Long actionMesageId) {
        this.actionMesageId = actionMesageId;
    }

    public Set<Team> getReceivedRequests() {
        return receivedRequests;
    }

    public void addRequest(Team team) {
        receivedRequests.add(team);
    }

    public void removeRequest(Team team) {
        receivedRequests.remove(team);
    }

    public void clearRequests() {
        receivedRequests.clear();
    }
}
