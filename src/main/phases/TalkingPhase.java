package phases;

import java.sql.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

import discordentities.DiscordTeam;
import discordentities.checkers.MessageWithPrivilegeUserChecker;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import phases.abstracts.APhase;
import phases.abstracts.IPhase;

// Phase 3. This phase is the longest one: teams are deciding what to do next.
// During the phase, teams are communicating inside and outside.
public class TalkingPhase extends APhase {
    private ArrayList<DiscordTeam> teams;
    private ArrayList<MessageWithPrivilegeUserChecker> controlMessages;
    private ArrayList<ArrayList<RequestStatus>> requests; // stores like a table: requests[homeTeam][guestTeam]
    // private Game game;

    private enum RequestStatus {
        NO, SENT, APPROVED
    }

    public TalkingPhase(JDA jda, ArrayList<DiscordTeam> teams) {
        super(jda);
        this.teams = teams;
        requests = new ArrayList<>(this.teams.size());
        for (int i = 0; i < this.teams.size(); i++) {
            ArrayList<RequestStatus> temp = new ArrayList<>(this.teams.size());
            for (int j = 0; j < this.teams.size(); j++)
                temp.add(RequestStatus.NO);
            requests.add(temp);
        }

        sendInfos().complete();
        sendControlMessages();
    }

    private RestAction<Void> sendInfos() {
        List<RestAction<Message>> messages = new ArrayList<>(teams.size());
        for (int i = 0; i < teams.size(); i++) {
            DiscordTeam team = teams.get(i);
            VoiceChannel vc = getJDA().getVoiceChannelById(team.getVoiceChannel().getChannelId());
            messages.add(vc.sendMessage(team.getFullName() + " info [DEBUG] [WILL DONE LATER]"));
        }
        return RestAction.allOf(messages).map(v -> null);
    }

    private MessageEmbed buildEmbed(int teamIndex) {
        StringJoiner send = new StringJoiner("\n");
        StringJoiner received = new StringJoiner("\n");
        for (int i = 0; i < this.teams.size(); i++) {
            if (i == teamIndex)
                continue;
            if (requests.get(teamIndex).get(i).equals(RequestStatus.SENT)) { // requests[home][i] == true ->
                send.add(teams.get(i).getFullName());
            }
            if (requests.get(i).get(teamIndex).equals(RequestStatus.SENT)) {
                received.add(teams.get(i).getFullName());
            }
        }
        return new EmbedBuilder()
            .setTitle("game_name [debug]")
            .addField("Send requests to:", send.toString(), false)
            .addField("Received requests from:", received.toString(), false)
            .build();
    }

    private void sendControlMessages() {
        List<RestAction<Message>> messages = new ArrayList<>(teams.size());
        for (int i = 0; i < teams.size(); i++) {
            DiscordTeam team = teams.get(i);
            VoiceChannel vc = getJDA().getVoiceChannelById(team.getVoiceChannel().getChannelId());
            messages.add(vc.sendMessageEmbeds(embed).flatMap(m));
        }
        return RestAction.allOf(messages).map(v -> null);
    }

    @Override
    public IPhase nextPhase() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'nextPhase'");
    }

}
