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
    private final Map<DiscordTeam, Messages> teamToMessages;
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
        teamToMessages = new HashMap<>(logic.getTeams().size());
        teamToForeingAmbassadorId = new HashMap<>(logic.getTeams().size());

        for (DiscordTeam team : logic.getTeams()) {
            channelToTeam.put(team.getProperty().voiceChatId(), team);
        }

        // UI
        sendPolls(logic.getTeams()).complete();

        // TODO: remove debug button
        {
            MessageCreateData message = new MessageCreateBuilder()
                    .addActionRow(Button.of(ButtonStyle.PRIMARY, "next_phase", "[DEBUG] Next phase"))
                    .build();
            getJDA().getVoiceChannelById(logic.getTeams().get(0).getProperty().voiceChatId())
                    .sendMessage(message).complete();
        }
    }

    private RestAction<?> sendPolls(List<DiscordTeam> teams) {
        List<RestAction<?>> actions = new ArrayList<>(teams.size());
        for (int i = 0; i < teams.size(); i++) {
            final DiscordTeam team = teams.get(i);
            Builder menuBuilder = createSelectCountyMenu(teams, team);

            MessageCreateData message = new MessageCreateBuilder()
                    .setContent("Select to which country to send a delegation")
                    .addActionRow(menuBuilder.build()).build();

            MessageCreateAction sendMessage = getJDA()
                    .getVoiceChannelById(teams.get(i).getProperty().voiceChatId())
                    .sendMessage(message);
            actions.add(sendMessage.onSuccess(msg -> teamToMessages.put(team, new Messages(msg.getIdLong(), 0))));

        }
        return RestAction.allOf(actions);
    }

    private Builder createSelectCountyMenu(List<DiscordTeam> teams, DiscordTeam team) {
        Builder menuBuilder = StringSelectMenu.create(DELEGATION_REQ);
        for (int j = 0; j < teams.size(); j++) {
            if (team == teams.get(j)) {
                continue;
            }
            CountryDescription desc = teams.get(j).getDescription();
            menuBuilder.addOption(desc.getName(), String.valueOf(j), desc.getEmoji());
        }

        return menuBuilder;
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
            editMessage(recipientChannelId, teamToMessages.get(recipientTeam).acceptDelegationMsgId,
                    createTextReplaceMessage("Waiting for delegation..."));

            // Requester side
            editMessage(requesterChannelId, teamToMessages.get(requesterTeam).requestDelegationMsgId,
                    createSelectAmbassadorMenu(requesterTeam.getMembers()));

        } else if (buttonId.equals(denyDelegation.getId())) {
            // Recipient side
            Messages recipientTeamMsgs = teamToMessages.get(recipientTeam);
            deleteMessage(recipientChannelId, recipientTeamMsgs.acceptDelegationMsgId);
            recipientTeamMsgs.acceptDelegationMsgId = 0;

            // Requester side
            editMessage(requesterChannelId, teamToMessages.get(requesterTeam).requestDelegationMsgId,
                    createTextReplaceMessage(recipientTeam.getDescription().getFullName() + " denied your request")
                            .setActionRow(createSelectCountyMenu(logic.getTeams(), requesterTeam).build()));
        } else if (buttonId.equals(kickDelegation.getId())) {
            endOfDelegation(requesterTeam, recipientTeam);
        }
        // TODO: remove debug button
        else if (buttonId.equals("next_phase")) {
            cancelTimer();
            phaseEnding();
        }
    }

    private void endOfDelegation(DiscordTeam requesterTeam, DiscordTeam recipientTeam) {
        long ambassadorId = teamToForeingAmbassadorId.get(recipientTeam);
        lock.lock();
        {
            teamToForeingAmbassadorId.remove(recipientTeam);
        }
        lock.unlock();

        Messages recipientTeamMsgs = teamToMessages.get(recipientTeam);
        deleteMessage(recipientTeam.getProperty().voiceChatId(),
                recipientTeamMsgs.acceptDelegationMsgId);
        recipientTeamMsgs.acceptDelegationMsgId = 0;

        VoiceChannel requesterVoiceChannel = getJDA()
                .getVoiceChannelById(requesterTeam.getProperty().voiceChatId());
        Guild guild = getJDA().getGuildById(session.getIODevice().getGuildId());
        guild.moveVoiceMember(guild.getMemberById(ambassadorId), requesterVoiceChannel).queue();

    }

    private void editMessage(long channelId, long msgId, MessageEditBuilder editData) {
        editMessage(channelId, msgId, editData.build());
    }

    private void editMessage(long channelId, long msgId, MessageEditData editData) {
        getJDA().getVoiceChannelById(channelId).editMessageById(msgId, editData).queue();
    }

    private void deleteMessage(long channelId, long msgId) {
        if (msgId != 0) {
            getJDA().getVoiceChannelById(channelId).deleteMessageById(msgId).queue();
        }
    }

    private MessageEditBuilder createSelectAmbassadorMenu(Set<IMember> members) {
        return createSelectAmbassadorMenu(new MessageEditBuilder(), members);
    }

    private MessageEditBuilder createSelectAmbassadorMenu(MessageEditBuilder builder, Set<IMember> members) {
        Builder menuBuilder = StringSelectMenu.create(SELECT_AMBASSADOR);
        for (IMember member : members) {
            User user = getJDA().getUserById(member.getId());
            menuBuilder.addOption(user.getName(), user.getId());
        }
        String content = builder.getContent();
        return builder.setContent(content + (content.isEmpty() ? "" : "\n") + "Select ambassador")
                .setActionRow(menuBuilder.build());
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
            int recipientTeamId = Integer.parseInt(event.getValues().get(0));
            DiscordTeam recipientTeam = logic.getTeams().get(recipientTeamId);
            lock.lock();
            {
                if (teamToForeingAmbassadorId.containsKey(recipientTeam)) {
                    event.getHook().sendMessage("This team already has delegation").setEphemeral(true).queue();
                    return;
                }

                long acceptMessageId = getJDA().getVoiceChannelById(recipientTeam.getProperty().voiceChatId())
                        .sendMessage(
                                createAcceptDelegationMessage(requesterTeam.getDescription().getFullName()).build())
                        .complete().getIdLong();
                teamToMessages.get(recipientTeam).acceptDelegationMsgId = acceptMessageId;
                requesterToRecipient.put(requesterTeam, recipientTeam);
                recipientToRequester.put(recipientTeam, requesterTeam);
            }
            lock.unlock();

            // Requester
            event.getHook().editMessageById(teamToMessages.get(requesterTeam).requestDelegationMsgId,
                    createTextReplaceMessage("You send request to: " + recipientTeam.getDescription().getFullName())
                            .build())
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
            Messages requesterTeamMsgs = teamToMessages.get(requesterTeam);
            deleteMessage(requesterTeam.getProperty().voiceChatId(),
                    requesterTeamMsgs.requestDelegationMsgId);
            requesterTeamMsgs.requestDelegationMsgId = 0;

            VoiceChannel recipientVoiceChannel = getJDA()
                    .getVoiceChannelById(requesterToRecipient.get(requesterTeam).getProperty().voiceChatId());
            Guild guild = getJDA().getGuildById(session.getIODevice().getGuildId());
            guild.moveVoiceMember(guild.getMemberById(ambassadorId), recipientVoiceChannel).queue();

            // Recipient
            editMessage(recipientTeam.getProperty().voiceChatId(),
                    teamToMessages.get(recipientTeam).acceptDelegationMsgId,
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

    private static MessageEditBuilder createTextReplaceMessage(String s) {
        return createTextReplaceMessage(new MessageEditBuilder(), s);
    }

    private static MessageEditBuilder createTextReplaceMessage(MessageEditBuilder builder, String s) {
        return builder.setContent(s).setReplace(true);
    }

    private static final Button acceptDelegation = Button.of(ButtonStyle.SUCCESS, "accept_delegation", "Accept");
    private static final Button denyDelegation = Button.of(ButtonStyle.DANGER, "deny_delegation", "Deny");

    private MessageCreateBuilder createAcceptDelegationMessage(String countyName) {
        return new MessageCreateBuilder()
                .setContent(countyName + " requests permission to delegate")
                .addActionRow(acceptDelegation, denyDelegation);
    }

    private static final Button kickDelegation = Button.of(ButtonStyle.DANGER, "kick_delegation", "Kick delegation");

    private MessageEditBuilder createKickDelegationMessage() {
        return new MessageEditBuilder().setActionRow(kickDelegation).setReplace(true);
    }

    @Override
    public void phaseEnding() {
        Guild guild = getJDA().getGuildById(session.getIODevice().getGuildId());
        for (DiscordTeam team : logic.getTeams()) {
            VoiceChannel voiceChannel = getJDA().getVoiceChannelById(team.getProperty().voiceChatId());
            for (IMember member : team.getMembers()) {
                try {
                    guild.moveVoiceMember(guild.getMemberById(member.getId()), voiceChannel).queue();
                } catch (Exception ignored) {
                }
            }
        }

        for (var teamMessages : teamToMessages.entrySet()) {
            if (teamMessages.getValue() != null) {
                deleteMessage(teamMessages.getKey().getProperty().voiceChatId(),
                        teamMessages.getValue().requestDelegationMsgId);
                deleteMessage(teamMessages.getKey().getProperty().voiceChatId(),
                        teamMessages.getValue().acceptDelegationMsgId);
            }
        }
        nextPhase();
    }

    @Override
    public int getDurationInMilliseconds() { return 0; }

    @Override
    public void nextPhase() {}

}
