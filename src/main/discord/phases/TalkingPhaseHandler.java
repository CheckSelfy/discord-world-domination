package discord.phases;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import discord.DiscordIODevice;
import discord.entities.DiscordTeam;
import game.Game;
import languages.CountryDescription;
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
import social_logic.Session;
import social_logic.phases.handlers_interfaces.ITalkingPhaseEventHandler;
import social_logic.phases.logic.TalkingPhaseLogic;

public class TalkingPhaseHandler extends ADiscordPhaseEventHandler
        implements ITalkingPhaseEventHandler {
    private static final String delegationReq = "delegationReq";

    private final TalkingPhaseLogic<DiscordTeam> logic;
    private final Map<Long, DiscordTeam> channelToTeam;

    public TalkingPhaseHandler(Session<DiscordIODevice, IDiscordPhaseEventHandler> session, Game<DiscordTeam> game) {
        super(session);
        logic = new TalkingPhaseLogic<>(this, game);
        channelToTeam = new HashMap<>(logic.getTeams().size());
        for (DiscordTeam team : logic.getTeams()) {
            channelToTeam.put(team.getProperty().voiceChatID(), team);
        }

        // UI
        sendPolls(game.getCountries()).complete();
    }

    // TODO: add single choise
    private RestAction<?> sendPolls(List<DiscordTeam> teams) {
        List<MessageCreateAction> actions = new ArrayList<>(teams.size());
        for (int i = 0; i < teams.size(); i++) {
            Builder menuBuilder = StringSelectMenu.create(delegationReq + i);
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

            MessageCreateAction sendMessage = getJDA()
                    .getVoiceChannelById(teams.get(i).getProperty().voiceChatID())
                    .sendMessage(message);
            actions.add(sendMessage);
        }
        return RestAction.allOf(actions);
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        //
        //
        //
    }

    @Override
    public void onStringSelectInteraction(StringSelectInteractionEvent event) {
        int recipientTeamId = Integer.parseInt(event.getValues().get(0));
        System.out.println(recipientTeamId);
        event.deferEdit().queue();
        DiscordTeam senderTeam = channelToTeam.get(event.getChannel().asVoiceChannel().getIdLong());
        getJDA().getVoiceChannelById(logic.getTeams().get(recipientTeamId).getProperty().voiceChatID())
                .sendMessage(createAcceptDelegationMessage(senderTeam.getDescription().getFullName())).queue();
        event.getHook().sendMessage("Invite send").setEphemeral(true).queue();
    }

    private static final Button acceptDelegation = Button.of(ButtonStyle.SUCCESS, "accept_delegation", "Accept");
    private static final Button denyDelegation = Button.of(ButtonStyle.DANGER, "deny_delegation", "Accept");

    private MessageCreateData createAcceptDelegationMessage(String countyName) {
        return new MessageCreateBuilder()
                .setContent(countyName + " requests permission to delegate")
                .addActionRow(acceptDelegation)
                .addActionRow(denyDelegation).build();
    }

    @Override
    public void phaseEnding() {}

    @Override
    public int getDurationInMilliseconds() { return 1000000000; }

    @Override
    public void nextPhase() {}

}
