package discord.phases;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import discord.DiscordIODevice;
import discord.entities.DiscordTeam;
import game.Game;
import game.actions.IAction;
import game.entities.City;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu.Builder;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import social_logic.Session;
import social_logic.phases.handlers_interfaces.IOrderPhaseEventHandler;
import social_logic.phases.logic.OrderPhaseLogic;

public class OrderPhaseEventHandler extends ADiscordPhaseEventHandler implements IOrderPhaseEventHandler {
    private final OrderPhaseLogic<DiscordTeam> phaseLogic;
    private final long[] menusMesasagesID;

    public OrderPhaseEventHandler(Session<DiscordIODevice, IDiscordPhaseEventHandler> session,
            Game<DiscordTeam> game) {
        super(session);
        this.phaseLogic = new OrderPhaseLogic<>(this, game);
        this.menusMesasagesID = new long[game.getCountries().size()];

        sendPolls().complete();
    }

    static final String chooseActionID = "chooseAction";
    static final String upgradeID = "upgrade"; // ID + country + city
    static final String shieldID = "shield"; // ID + country + city
    static final String developNuclearID = "devnuclear"; // ID + country
    static final String createBombID = "createBomb"; // ID + country
    static final String bombCityID = "bomb"; // ID + countrySender + countryReceiver + cityReceiver
    static final String ecologyID = "ecology"; // ID + country
    static final String sanctionsID = "sanctions"; // ID + countrySender + countryReceiver
    static final String unsanctionsID = "unsanctions"; // ID + countrySender + countryReceiver
    static final String transferMoneyID = "transferMoney"; // ID + countrySender + countryReceiver

    private StringSelectMenu getActionMenu(int indexOfTeam) {
        return StringSelectMenu.create(chooseActionID + indexOfTeam)
                .addOption("Country actions", "country_actions")
                .addOption("Nuclear actions", "nuclear_actions")
                .addOption("Ecology actions", "ecology_actions")
                .addOption("Economy actions", "economy_actions")
                .build();
    }

    private RestAction<?> sendPolls() {
        List<RestAction<?>> actions = new ArrayList<>();
        for (int i = 0; i < phaseLogic.getTeams().size(); i++) {
            int index = i;

            StringSelectMenu chooseActionMenu = getActionMenu(i);
            DiscordTeam team = phaseLogic.getTeams().get(index);

            MessageCreateData messageData = new MessageCreateBuilder()
                    .setContent("[DEBUG] Menu")
                    .addActionRow(chooseActionMenu)
                    .build();

            VoiceChannel vc = getJDA().getVoiceChannelById(team.getProperty().voiceChatId());

            actions.add(vc.sendMessage(messageData).onSuccess(msg -> menusMesasagesID[index] = msg.getIdLong()));
        }
        return RestAction.allOf(actions);
    }

    private List<ActionRow> countryMenu(int index) {
        DiscordTeam team = phaseLogic.getTeams().get(index);

        List<Button> upgradeButtons = new ArrayList<>(4);
        List<Button> shieldButtons = new ArrayList<>(4);
        for (int i = 0; i < 4; i++) {
            City city = team.getCities()[i];
            Button upgradeButton = Button.of(ButtonStyle.SUCCESS, "upgrade" + index + i,
                    "Upgrade " + team.getDescription().getCityNames()[i]);
            Button shieldButton = Button.of(ButtonStyle.SUCCESS, "shield" + index + i,
                    "Shield " + team.getDescription().getCityNames()[i]);
            upgradeButtons
                    .add(city.isAlive() ? upgradeButton.asEnabled() : upgradeButton.asDisabled());
            shieldButtons
                    .add(city.isAlive() && !city.isShielded() ? shieldButton.asEnabled() : shieldButton.asDisabled());
        }

        return List.of(ActionRow.of(upgradeButtons), ActionRow.of(shieldButtons));
    }

    private StringSelectMenu getCitiesToBomb(int indexOfTeam) {
        Builder builder = StringSelectMenu.create(bombCityID + indexOfTeam);
        int missiles = phaseLogic.getTeams().get(indexOfTeam).getMissiles();
        for (int i = 0; i < phaseLogic.getTeams().size(); i++) {
            if (i == indexOfTeam) {
                continue;
            }

            DiscordTeam team = phaseLogic.getTeams().get(i);
            for (int cityId = 0; cityId < team.getCities().length; cityId++) {
                if (team.getCities()[cityId].isAlive()) {
                    builder.addOption(
                            team.getDescription().getCityNames()[cityId],
                            bombCityID + i + cityId,
                            team.getDescription().getEmoji());
                }
            }
        }

        if (missiles == 0) {
            return builder.setDisabled(true).build();
        } else {
            return builder.setMaxValues(missiles).build();
        }
    }

    private List<ActionRow> nuclearMenu(int index) {
        DiscordTeam team = phaseLogic.getTeams().get(index);
        if (team.hasNuclear()) {
            return List.of(
                    ActionRow.of(
                            Button.of(ButtonStyle.PRIMARY, createBombID + "1" + index, "Create 1 bomb"),
                            Button.of(ButtonStyle.PRIMARY, createBombID + "2" + index, "Create 2 bombs"),
                            Button.of(ButtonStyle.PRIMARY, createBombID + "3" + index, "Create 3 bombs")),
                    ActionRow.of(
                            getCitiesToBomb(index)));
        } else {
            return List.of(ActionRow.of(Button.of(ButtonStyle.PRIMARY, developNuclearID + index, "Develop nuclear")));
        }
    }

    // TODO bad naming. (and refactor it)
    // This method creates StringSelectMenu:
    // * if doSanctions == true, then it's creates menu with countries, which
    // current country can impose sanctions
    // * if doSanctions == false, then it's creates menu with countries, which
    // current country can cancel sanctions
    // Returns null, if stringselectmenu is empty.
    private StringSelectMenu getCountriesToSanctions(int indexOfTeam, boolean doSanctions) {
        DiscordTeam team = phaseLogic.getTeams().get(indexOfTeam);
        Builder builder = StringSelectMenu.create((doSanctions ? sanctionsID : unsanctionsID) + indexOfTeam);
        for (int i = 0; i < phaseLogic.getTeams().size(); i++) {
            if (i == indexOfTeam) {
                continue;
            }

            DiscordTeam curTeam = phaseLogic.getTeams().get(i);

            if (doSanctions != curTeam.isSanctionsImposed(team)) {
                builder.addOption(
                        curTeam.getDescription().getName(),
                        doSanctions ? sanctionsID + indexOfTeam + i : unsanctionsID + indexOfTeam + i,
                        curTeam.getDescription().getEmoji());
            }
            System.out.println();
        }

        return builder.getOptions().isEmpty() ? null : builder.build();
    }

    private StringSelectMenu getCountriesToGiveMoney(int indexOfTeam) {
        DiscordTeam team = phaseLogic.getTeams().get(indexOfTeam);
        Builder builder = StringSelectMenu.create(transferMoneyID + indexOfTeam);
        for (int i = 0; i < phaseLogic.getTeams().size(); i++) {
            if (i == indexOfTeam) {
                continue;
            }

            DiscordTeam curTeam = phaseLogic.getTeams().get(i);

            builder.addOption(
                    curTeam.getDescription().getName(),
                    transferMoneyID + indexOfTeam + i,
                    curTeam.getDescription().getEmoji());
            System.out.println();
        }

        return builder.getOptions().isEmpty() ? null : builder.build();

    }

    // TODO transferMoney
    private List<ActionRow> economicMenu(int index) {
        StringSelectMenu imposeSanctions = getCountriesToSanctions(index, true);
        StringSelectMenu unimposeSanctions = getCountriesToSanctions(index, false);
        List<ActionRow> result = new ArrayList<>(2);
        System.out.println("imposeSanctions == null ? " + (imposeSanctions == null));
        if (imposeSanctions != null) {
            result.add(ActionRow.of(imposeSanctions));
        }

        System.out.println("unimposeSanctions == null ? " + (unimposeSanctions == null));
        if (unimposeSanctions != null) {
            result.add(ActionRow.of(unimposeSanctions));
        }

        result.add(ActionRow.of(getCountriesToGiveMoney(index)));

        return result;
    }

    private List<ActionRow> ecologicMenu(int index) {
        return List.of(ActionRow.of(Button.of(ButtonStyle.PRIMARY, ecologyID + index, "improve ecology")));
    }

    // TODO add check for president
    @Override
    public void onStringSelectInteraction(StringSelectInteractionEvent event) {
        String id = event.getComponent().getId();
        if (id.startsWith(chooseActionID)) {
            event.deferEdit().queue();
            int indexOfTeam = Integer.parseInt(id.substring(chooseActionID.length()));
            List<ActionRow> newMenu = new ArrayList<>();
            newMenu.add(ActionRow.of(getActionMenu(indexOfTeam)));
            newMenu.addAll(switch (event.getValues().get(0)) {
                case "country_actions" -> countryMenu(indexOfTeam);
                case "nuclear_actions" -> nuclearMenu(indexOfTeam);
                case "ecology_actions" -> ecologicMenu(indexOfTeam);
                case "economy_actions" -> economicMenu(indexOfTeam);
                default -> throw new RuntimeException("Unknow option " + event.getValues().get(0));
            });

            event.getChannel().editMessageComponentsById(event.getMessageIdLong(), newMenu).queue();
        }
    }

    private String getOrderActions(int index) {
        StringBuilder builder = new StringBuilder();

        Iterator<IAction> i = phaseLogic.getIterator(index);
        while (i.hasNext()) {
            IAction action = i.next();
            builder.append(action.getClass()).append("\n");
        }

        return builder.toString();
    }

    @Override
    public void phaseEnding() { throw new UnsupportedOperationException("Unimplemented method 'phaseEnding'"); }

    @Override
    public int getDurationInMilliseconds() { return 1000 * 60 * 3; }

    @Override
    public void nextPhase() { throw new UnsupportedOperationException("Unimplemented method 'nextPhase'"); }

}
