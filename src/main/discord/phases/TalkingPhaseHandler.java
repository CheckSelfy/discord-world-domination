package discord.phases;

import java.util.ArrayList;
import java.util.List;

import discord.DiscordIODevice;
import discord.entities.DiscordTeam;
import languages.CountryDescription;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
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
    private TalkingPhaseLogic<DiscordTeam> logic;

    private static final String pickPresident = "delegationReq";

    public TalkingPhaseHandler(Session<DiscordIODevice, IDiscordPhaseEventHandler> session,
            List<DiscordTeam> teams) {
        super(session);
        logic = new TalkingPhaseLogic<>(teams);

        // UI
        sendPolls(teams).complete();
    }

    private RestAction<?> sendPolls(List<DiscordTeam> teams) {
        List<MessageCreateAction> actions = new ArrayList<>(teams.size());
        for (int i = 0; i < teams.size(); i++) {
            Builder menuBuilder = StringSelectMenu.create(pickPresident + i);
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
        //
        //
        //
    }

    @Override
    public void phaseEnding() {}

    @Override
    public int getDurationInMilliseconds() { return 0; }

    @Override
    public void nextPhase() {}

}
