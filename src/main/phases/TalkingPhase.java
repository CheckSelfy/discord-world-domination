package phases;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.regex.Pattern;

import discordentities.DiscordTeam;
import discordentities.checkers.MessageWithPrivilegeUserChecker;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.MessageEditAction;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;
import net.dv8tion.jda.api.utils.messages.MessageEditData;
import phases.abstracts.APhase;
import phases.abstracts.IPhase;
import util.Constants;
import util.GameUtil;
import util.Pair;

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

        controlMessages = new ArrayList<>(this.teams.size());
        for (int i = 0; i < this.teams.size(); i++) {
            controlMessages.add(null);
        }

        sendInfos().flatMap(v -> sendControlMessages()).queue();
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
        StringJoiner youCanJoin = new StringJoiner("\n");
        StringJoiner canJoinToYou = new StringJoiner("\n");
        for (int i = 0; i < this.teams.size(); i++) {
            if (i == teamIndex)
                continue;
            if (requests.get(teamIndex).get(i).equals(RequestStatus.SENT)) {
                send.add(teams.get(i).getFullName());
            }
            if (requests.get(i).get(teamIndex).equals(RequestStatus.SENT)) {
                received.add(teams.get(i).getFullName());
            }
            if (requests.get(teamIndex).get(i).equals(RequestStatus.APPROVED)) {
                youCanJoin.add(teams.get(i).getFullName());
            }
            if (requests.get(i).get(teamIndex).equals(RequestStatus.APPROVED)) {
                canJoinToYou.add(teams.get(i).getFullName());
            }
        }
        return new EmbedBuilder().setTitle("game_name [debug]").addField("Send requests to:", send.toString(), false)
                .addField("Received requests from:", received.toString(), false)
                .addField("These teams can join to you:", canJoinToYou.toString(), false)
                .addField("You can join to these teams:", youCanJoin.toString(), false).build();
    }

    // button = [send\receive] + <object> + "_" + <subject>
    private List<ActionRow> buildButtons(int teamIndex) {
        List<Button> send = new ArrayList<>(this.teams.size() - 1);
        List<Button> receive = new ArrayList<>(this.teams.size() - 1);
        List<Button> join = new ArrayList<>(this.teams.size() - 1);
        for (int i = 0; i < this.teams.size(); i++) {
            if (i == teamIndex) {
                continue;
            }

            Button sendButton = Button.of(ButtonStyle.PRIMARY, "send" + teamIndex + "_" + i, "Send",
                    teams.get(i).getEmoji());
            Button receiveButton = Button.of(ButtonStyle.PRIMARY, "receive" + teamIndex + "_" + i, "Approve",
                    teams.get(i).getEmoji());
            Button joinButton = Button.of(ButtonStyle.PRIMARY, "join" + teamIndex + "_" + i, "Join",
                    teams.get(i).getEmoji());

            if (requests.get(teamIndex).get(i).equals(RequestStatus.NO)) {
                sendButton = sendButton.asEnabled();
            } else {
                sendButton = sendButton.asDisabled();
            }

            if (requests.get(i).get(teamIndex).equals(RequestStatus.SENT)) {
                receiveButton = receiveButton.asEnabled();
            } else {
                receiveButton = receiveButton.asDisabled();
            }

            if (requests.get(teamIndex).get(i).equals(RequestStatus.APPROVED)) {
                joinButton = joinButton.asEnabled();
            } else {
                joinButton = joinButton.asDisabled();
            }

            send.add(sendButton);
            receive.add(receiveButton);
            join.add(joinButton);
        }
        return List.of(ActionRow.of(send), ActionRow.of(receive), ActionRow.of(join));
    }

    private RestAction<Void> sendControlMessages() {
        List<RestAction<Void>> messages = new ArrayList<>(teams.size());
        for (int i = 0; i < teams.size(); i++) {
            int index = i;
            DiscordTeam team = teams.get(i);
            VoiceChannel vc = getJDA().getVoiceChannelById(team.getVoiceChannel().getChannelId());
            MessageCreateData data = new MessageCreateBuilder().setEmbeds(buildEmbed(i)).setComponents(buildButtons(i))
                    .build();
            messages.add(vc.sendMessage(data).flatMap(msg -> {
                controlMessages.set(index, new MessageWithPrivilegeUserChecker(msg.getGuild().getIdLong(),
                        msg.getChannel().getIdLong(), msg.getIdLong(), team.getPresident().getUserId()));
                return msg.addReaction(Emoji.fromFormatted(Constants.bundle.getString("join_delegate_emoji")));
            }).map(v -> null));
        }
        return RestAction.allOf(messages).map(v -> null);
    }

    private Pair<Integer, Integer> parseButtonTeams(String str) {
        int left, right;
        StringBuilder s = new StringBuilder();
        int index = 0;
        while (index < str.length() && str.charAt(index) != '_') {
            s.append(str.charAt(index));
            index++;
        }
        left = Integer.parseInt(s.toString());
        index++;
        s = new StringBuilder();
        while (index < str.length() && str.charAt(index) != '_') {
            s.append(str.charAt(index));
            index++;
        }
        right = Integer.parseInt(s.toString());
        return new Pair<Integer, Integer>(left, right);
    }

    private MessageEditAction updateMessage(int index) {
        DiscordTeam team = teams.get(index);
        VoiceChannel vc = getJDA().getVoiceChannelById(team.getVoiceChannel().getChannelId());
        MessageEditData data = new MessageEditBuilder().setEmbeds(buildEmbed(index)).setComponents(buildButtons(index))
                .build();
        return vc.editMessageById(controlMessages.get(index).getMessageId(), data);
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        Button button = event.getButton();
        String str = button.getId();
        System.out.println(str + "clicked");
        if (str.startsWith("send")) {
            str = str.substring("send".length());
            Pair<Integer, Integer> parsedIndexes = parseButtonTeams(str);
            int guestTeam = parsedIndexes.getFirst(); // it's us.
            int homeTeam = parsedIndexes.getSecond();
            if (!teams.get(guestTeam).isPresident(event.getUser().getIdLong())) {
                event.reply("You are not the president.").setEphemeral(true).queue();
                return;
            }
            requests.get(guestTeam).set(homeTeam, RequestStatus.SENT);
            System.out.println("Send request from '" + teams.get(homeTeam).getName() + "' to '"
                    + teams.get(guestTeam).getName() + "'");
            event.reply("Ok!").setEphemeral(true).flatMap(v -> updateMessage(homeTeam))
                    .flatMap(m -> updateMessage(guestTeam)).queue();
        } else if (str.startsWith("receive")) {
            str = str.substring("receive".length());
            Pair<Integer, Integer> parsedIndexes = parseButtonTeams(str);
            int guestTeam = parsedIndexes.getFirst(); 
            int homeTeam = parsedIndexes.getSecond(); // it's us
            if (!teams.get(guestTeam).isPresident(event.getUser().getIdLong())) {
                event.reply("You are not the president.").setEphemeral(true).queue();
                return;
            }
            requests.get(homeTeam).set(guestTeam, RequestStatus.APPROVED);
            System.out.println("Accepted request from '" + teams.get(guestTeam).getName() + "' to '"
                    + teams.get(homeTeam).getName() + "'");
            event.reply("Ok!").setEphemeral(true).flatMap(v -> updateMessage(homeTeam))
                    .flatMap(m -> updateMessage(guestTeam)).queue();
        } else if (str.startsWith("join")) {
            str = str.substring("send".length());
            Pair<Integer, Integer> parsedIndexes = parseButtonTeams(str);
            int guestTeam = parsedIndexes.getFirst(); // it's us
            int homeTeam = parsedIndexes.getSecond(); 
            if (!teams.get(guestTeam).isPresident(event.getUser().getIdLong())) {
                event.reply("You are not the president.").setEphemeral(true).queue();
                return;
            }

            VoiceChannel homeTeamVC = getJDA().getVoiceChannelById(teams.get(homeTeam).getVoiceChannel().getChannelId());

            event.getChannel().asVoiceChannel().retrieveMessageById(event.getMessageIdLong()).flatMap(
                msg -> msg.getReaction(Emoji.fromFormatted(Constants.bundle.getString("join_delegate_emoji"))).retrieveUsers())
            .flatMap(listOfUsers -> {
                listOfUsers.removeIf(user -> user.isBot());
                if (listOfUsers.isEmpty()) {
                    return event.reply("Pick emoji [debug]");
                }
                List<RestAction<Void>> actions = new ArrayList<>();
                Guild guild = event.getGuild();
                System.out.println("Moving " + listOfUsers);
                for (User user: listOfUsers) {
                    actions.add(
                    guild.moveVoiceMember(
                        guild.getMemberById(user.getIdLong()),
                        homeTeamVC
                    ));
                }
                return event.reply("Joining...").setEphemeral(true).flatMap(v ->
                    RestAction.allOf(actions)
                ).flatMap(v -> null);
            }).queue();
        }
    }

    @Override
    public IPhase nextPhase() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'nextPhase'");
    }

}
