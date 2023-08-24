package discord.phases;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import discord.DiscordIODevice;
import discord.entities.DiscordTeam;
import game.Game;
import languages.CountryDescription;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
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
    private final Map<DiscordTeam, DiscordTeam> recipientToRequester;
    private final Map<DiscordTeam, Long> teamToMessageID;

    public TalkingPhaseHandler(Session<DiscordIODevice, IDiscordPhaseEventHandler> session, Game<DiscordTeam> game) {
        super(session);
        logic = new TalkingPhaseLogic<>(this, game);
        channelToTeam = new HashMap<>(logic.getTeams().size());
        recipientToRequester = new HashMap<>(logic.getTeams().size());
        teamToMessageID = new HashMap<>(logic.getTeams().size());

        for (DiscordTeam team : logic.getTeams()) {
            channelToTeam.put(team.getProperty().voiceChatID(), team);
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
                    .getVoiceChannelById(teams.get(i).getProperty().voiceChatID())
                    .sendMessage(message);
            actions.add(sendMessage.onSuccess(msg -> teamToMessageID.put(team, msg.getIdLong())));

        }
        return RestAction.allOf(actions);
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        event.deferEdit().queue();
        DiscordTeam recipientTeam = channelToTeam.get(event.getChannel().asVoiceChannel().getIdLong());
        DiscordTeam senderTeam = recipientToRequester.get(recipientTeam);
        long senderChannelId = senderTeam.getProperty().voiceChatID();
        if (event.getButton().getId().equals(acceptDelegation.getId())) {
            getJDA().getVoiceChannelById(senderChannelId)
                    .editMessageById(teamToMessageID.get(senderTeam),
                            createSelectAmbassadorMenu(senderTeam.getMembers()))
                    .queue();
        } else if (event.getButton().getId().equals(denyDelegation.getId())) {
            getJDA().getVoiceChannelById(senderChannelId)
                    .editMessageById(teamToMessageID.get(senderTeam),
                            createTextReplaceMessage(
                                    recipientTeam.getDescription().getFullName() + " denied your request"))
                    .queue();
        }
    }

    private MessageEditData createSelectAmbassadorMenu(Set<IMember> members) {
        Builder menuBuilder = StringSelectMenu.create(SELECT_AMBASSADOR);
        for (var member : members) {
            User user = getJDA().getUserById(member.getID());
            menuBuilder.addOption(user.getName(), user.getId());
        }
        return new MessageEditBuilder().setContent("Select ambassador")
                .setActionRow(menuBuilder.build())
                .build();
    }

    @Override
    public void onStringSelectInteraction(StringSelectInteractionEvent event) {
        String menuId = event.getSelectMenu().getId();
        if (menuId.startsWith(DELEGATION_REQ)) {
            DiscordTeam senderTeam = channelToTeam.get(event.getChannel().asVoiceChannel().getIdLong());
            event.deferEdit().queue();
            if (event.getUser().getIdLong() != senderTeam.getPresident().getID()) {
                event.getHook().sendMessage("Only president can send requests").setEphemeral(true).queue();
                return;
            }

            if (recipientToRequester.containsValue(senderTeam)) {
                event.getHook().sendMessage("You can send only one request per round").setEphemeral(true).queue();
                return;
            }

            DiscordTeam recipientTeam = logic.getTeams().get(Integer.parseInt(event.getValues().get(0)));
            getJDA().getVoiceChannelById(recipientTeam.getProperty().voiceChatID())
                    .sendMessage(createAcceptDelegationMessage(senderTeam.getDescription().getFullName())).complete();
            recipientToRequester.put(recipientTeam, senderTeam);

            event.getHook().editMessageById(teamToMessageID.get(senderTeam),
                    createTextReplaceMessage("You send request to: " + recipientTeam.getDescription().getFullName()))
                    .queue();
        } else if (menuId.startsWith(SELECT_AMBASSADOR)) {
            //
        }
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

    @Override
    public void phaseEnding() {}

    @Override
    public int getDurationInMilliseconds() { return 0; }

    @Override
    public void nextPhase() {}

}
