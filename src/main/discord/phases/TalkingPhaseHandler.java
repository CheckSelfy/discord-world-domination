package discord.phases;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import discord.DiscordIODevice;
import discord.entities.DiscordTeam;
import game.Game;
import languages.CountryDescription;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu.Builder;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;
import net.dv8tion.jda.api.utils.messages.MessageEditData;
import social_logic.Session;
import social_logic.entities.IMember;
import social_logic.phases.handlers_interfaces.ITalkingPhaseEventHandler;
import social_logic.phases.logic.TalkingPhaseLogic;

public class TalkingPhaseHandler extends ADiscordPhaseEventHandler
        implements ITalkingPhaseEventHandler {
    private static final String DELEGATION_REQ = "delegation_req";
    private static final String SELECT_AMBASSADOR = "select_ambassador";

    private final TalkingPhaseLogic<DiscordTeam> logic;
    private final Map<Long, DiscordTeam> channelToTeam;
    private final Map<DiscordTeam, DiscordTeam> requesterToRecipient;
    private final Map<DiscordTeam, DiscordTeam> recipientToRequester;
    private final Map<DiscordTeam, Messages> teamToMessageId;
    private final Map<DiscordTeam, Long> teamToForeingAmbassadorId;

    private final Lock lock = new ReentrantLock(true);

    private class Messages {
        public long requestDelegationMsgId;
        public long acceptDelegationMsgId;

        public Messages(long requestDelegationMsgId, long acceptDelegationMsgId) {
            this.requestDelegationMsgId = requestDelegationMsgId;
            this.acceptDelegationMsgId = acceptDelegationMsgId;
        }
    }

    public TalkingPhaseHandler(Session<DiscordIODevice, IDiscordPhaseEventHandler> session, Game<DiscordTeam> game) {
        super(session);
        logic = new TalkingPhaseLogic<>(this, game);
        channelToTeam = new HashMap<>(logic.getTeams().size());
        requesterToRecipient = new HashMap<>(logic.getTeams().size());
        recipientToRequester = new HashMap<>(logic.getTeams().size());
        teamToMessageId = new HashMap<>(logic.getTeams().size());
        teamToForeingAmbassadorId = new HashMap<>(logic.getTeams().size());

        for (DiscordTeam team : logic.getTeams()) {
            channelToTeam.put(team.getProperty().voiceChatId(), team);
        }

        // UI
        sendPolls(logic.getTeams()).complete();
    }

    private RestAction<?> sendPolls(List<DiscordTeam> teams) {
        List<RestAction<?>> actions = new ArrayList<>(teams.size());
        for (int i = 0; i < teams.size(); i++) {
            Builder menuBuilder = StringSelectMenu.create(DELEGATION_REQ + i);
            for (int j = 0; j < teams.size(); j++) {
                if (i == j) {
                    continue;
                }
                CountryDescription desc = teams.get(j).getDescription();
                menuBuilder.addOption(desc.getName(), String.valueOf(j), desc.getEmoji());
            }

            MessageCreateData message = new MessageCreateBuilder()
                    .setContent("Select to which country to send a delegation")
                    .addActionRow(menuBuilder.build()).build();

            final DiscordTeam team = teams.get(i);
            MessageCreateAction sendMessage = getJDA()
                    .getVoiceChannelById(teams.get(i).getProperty().voiceChatId())
                    .sendMessage(message);
            actions.add(sendMessage.onSuccess(msg -> teamToMessageId.put(team, new Messages(msg.getIdLong(), 0))));

        }
        return RestAction.allOf(actions);
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        event.deferEdit().queue();

        DiscordTeam recipientTeam = channelToTeam.get(event.getChannel().asVoiceChannel().getIdLong());
        if (!validatePresident(event, recipientTeam.getPresident().getId())) {
            return;
        }

        long recipientChannelId = recipientTeam.getProperty().voiceChatId();
        DiscordTeam requesterTeam = recipientToRequester.get(recipientTeam);
        long requesterChannelId = requesterTeam.getProperty().voiceChatId();

        String buttonId = event.getButton().getId();
        if (buttonId.equals(acceptDelegation.getId())) {
            // Recipient side
            editMessage(recipientChannelId, teamToMessageId.get(recipientTeam).acceptDelegationMsgId,
                    createTextReplaceMessage("Waiting for delegation..."));

            // Requester side
            editMessage(requesterChannelId, teamToMessageId.get(requesterTeam).requestDelegationMsgId,
                    createSelectAmbassadorMenu(requesterTeam.getMembers()));

        } else if (buttonId.equals(denyDelegation.getId())) {
            // Recipient side
            Messages recipientTeamMsgs = teamToMessageId.get(recipientTeam);
            deleteMessage(recipientChannelId, recipientTeamMsgs.acceptDelegationMsgId);
            recipientTeamMsgs.acceptDelegationMsgId = 0;

            // Requester side
            editMessage(requesterChannelId, teamToMessageId.get(requesterTeam).requestDelegationMsgId,
                    createTextReplaceMessage(
                            recipientTeam.getDescription().getFullName() + " denied your request"));
        } else if (buttonId.equals(kickDelegation.getId())) {
            endOfDelegation(requesterTeam, recipientTeam);
        }
    }

    private void endOfDelegation(DiscordTeam requesterTeam, DiscordTeam recipientTeam) {
        long ambassadorId = teamToForeingAmbassadorId.get(recipientTeam);
        lock.lock();
        {
            teamToForeingAmbassadorId.remove(recipientTeam);
        }
        lock.unlock();

        Messages recipientTeamMsgs = teamToMessageId.get(recipientTeam);
        deleteMessage(recipientTeam.getProperty().voiceChatId(),
                recipientTeamMsgs.acceptDelegationMsgId);
        recipientTeamMsgs.acceptDelegationMsgId = 0;

        VoiceChannel requesterVoiceChannel = getJDA()
                .getVoiceChannelById(requesterTeam.getProperty().voiceChatId());
        Guild guild = getJDA().getGuildById(session.getIODevice().getGuildId());
        guild.moveVoiceMember(guild.getMemberById(ambassadorId), requesterVoiceChannel).queue();

    }

    private void editMessage(long channelId, long msgId, MessageEditData editData) {
        getJDA().getVoiceChannelById(channelId).editMessageById(msgId, editData).queue();
    }

    private void deleteMessage(long channelId, long msgId) {
        if (msgId != 0) {
            getJDA().getVoiceChannelById(channelId).deleteMessageById(msgId).queue();
        }
    }

    private MessageEditData createSelectAmbassadorMenu(Set<IMember> members) {
        Builder menuBuilder = StringSelectMenu.create(SELECT_AMBASSADOR);
        for (IMember member : members) {
            User user = getJDA().getUserById(member.getId());
            menuBuilder.addOption(user.getName(), user.getId());
        }
        return new MessageEditBuilder().setContent("Select ambassador")
                .setActionRow(menuBuilder.build())
                .build();
    }

    //

    @Override
    public void onStringSelectInteraction(StringSelectInteractionEvent event) {
        event.deferEdit().queue();

        DiscordTeam requesterTeam = channelToTeam.get(event.getChannel().asVoiceChannel().getIdLong());
        if (!validatePresident(event, requesterTeam.getPresident().getId())) {
            return;
        }

        String menuId = event.getSelectMenu().getId();
        if (menuId.startsWith(DELEGATION_REQ)) {
            if (requesterToRecipient.containsKey(requesterTeam)) {
                event.getHook().sendMessage("You can send only one request per round").setEphemeral(true).queue();
                return;
            }

            DiscordTeam recipientTeam = logic.getTeams().get(Integer.parseInt(event.getValues().get(0)));
            lock.lock();
            {
                if (teamToForeingAmbassadorId.containsKey(recipientTeam)) {
                    event.getHook().sendMessage("This team already has delegation").setEphemeral(true).queue();
                    return;
                }

                long acceptMessageId = getJDA().getVoiceChannelById(recipientTeam.getProperty().voiceChatId())
                        .sendMessage(createAcceptDelegationMessage(requesterTeam.getDescription().getFullName()))
                        .complete().getIdLong();
                teamToMessageId.get(recipientTeam).acceptDelegationMsgId = acceptMessageId;
                requesterToRecipient.put(requesterTeam, recipientTeam);
                recipientToRequester.put(recipientTeam, requesterTeam);
            }
            lock.unlock();

            event.getHook().editMessageById(teamToMessageId.get(requesterTeam).requestDelegationMsgId,
                    createTextReplaceMessage(
                            "You send request to: " + recipientTeam.getDescription().getFullName()))
                    .queue();
        } else if (menuId.startsWith(SELECT_AMBASSADOR)) {
            long ambassadorId = Long.parseLong(event.getValues().get(0));
            DiscordTeam recipientTeam = requesterToRecipient.get(requesterTeam);
            lock.lock();
            {
                teamToForeingAmbassadorId.put(recipientTeam, ambassadorId);
            }
            lock.unlock();

            // Requester
            Messages requesterTeamMsgs = teamToMessageId.get(requesterTeam);
            deleteMessage(requesterTeam.getProperty().voiceChatId(),
                    requesterTeamMsgs.requestDelegationMsgId);
            requesterTeamMsgs.requestDelegationMsgId = 0;

            VoiceChannel recipientVoiceChannel = getJDA()
                    .getVoiceChannelById(requesterToRecipient.get(requesterTeam).getProperty().voiceChatId());
            Guild guild = getJDA().getGuildById(session.getIODevice().getGuildId());
            guild.moveVoiceMember(guild.getMemberById(ambassadorId), recipientVoiceChannel).queue();

            // Recipient
            editMessage(recipientTeam.getProperty().voiceChatId(),
                    teamToMessageId.get(recipientTeam).acceptDelegationMsgId,
                    createKickDelegationMessage());
        }

    }

    private static boolean validatePresident(GenericComponentInteractionCreateEvent event, long presidentId) {
        if (event.getUser().getIdLong() != presidentId) {
            event.getHook().sendMessage("Only president can use controls").setEphemeral(true).queue();
            return false;
        }
        return true;
    }

    private static MessageEditData createTextReplaceMessage(String s) {
        return new MessageEditBuilder().setContent(s).setReplace(true).build();
    }

    private static final Button acceptDelegation = Button.of(ButtonStyle.SUCCESS, "accept_delegation", "Accept");
    private static final Button denyDelegation = Button.of(ButtonStyle.DANGER, "deny_delegation", "Deny");

    private MessageCreateData createAcceptDelegationMessage(String countyName) {
        return new MessageCreateBuilder()
                .setContent(countyName + " requests permission to delegate")
                .addActionRow(acceptDelegation, denyDelegation)
                .build();
    }

    private static final Button kickDelegation = Button.of(ButtonStyle.DANGER, "kick_delegation", "Kick delegation");

    private MessageEditData createKickDelegationMessage() {
        return new MessageEditBuilder().setActionRow(kickDelegation).setReplace(true).build();
    }

    @Override
    public void phaseEnding() {}

    @Override
    public int getDurationInMilliseconds() { return 0; }

    @Override
    public void nextPhase() {}

}
