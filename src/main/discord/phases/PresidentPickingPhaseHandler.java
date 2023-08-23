package discord.phases;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import discord.DiscordIODevice;
import discord.entities.DiscordMember;
import discord.entities.DiscordTeamProperty;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu.Builder;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.AuditableRestAction;
import net.dv8tion.jda.api.requests.restaction.ChannelAction;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import net.dv8tion.jda.api.requests.restaction.RoleAction;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import social_logic.Session;
import social_logic.entities.IMember;
import social_logic.entities.TeamBuilder;
import social_logic.phases.handlers_interfaces.IPresidentPickingPhaseEventHandler;
import social_logic.phases.logic.PresidentPickingPhaseLogic;
import util.Constants;

public class PresidentPickingPhaseHandler extends ADiscordPhaseEventHandler
        implements IPresidentPickingPhaseEventHandler {
    private final PresidentPickingPhaseLogic phaseLogic;
    private final ArrayList<DiscordTeamProperty> properties;

    public PresidentPickingPhaseHandler(Session<DiscordIODevice, IDiscordPhaseEventHandler> session,
            ArrayList<TeamBuilder> builders) {
        super(session);

        this.phaseLogic = new PresidentPickingPhaseLogic(this, builders);
        this.properties = new ArrayList<>(builders.size());
        for (int i = 0; i < builders.size(); i++) {
            properties.add(null);
        }

        createChannelsAndRoles().complete();
        sendPolls().complete();
        scheduleEnd();
    }

    private static final String pickPresident = "votePresident";

    private RestAction<?> sendPolls() {
        List<MessageCreateAction> actions = new ArrayList<>(phaseLogic.getTeamCount());
        for (int i = 0; i < phaseLogic.getTeamCount(); i++) {
            Set<IMember> members = phaseLogic.getTeamBuilder(i).getMembers();
            Builder menuBuilder = StringSelectMenu.create(pickPresident + i); // id of interaction
            for (IMember m : members) {
                User user = getJDA().getUserById(m.getID());
                menuBuilder.addOption(user.getName(), user.getId());
            }

            MessageCreateData message = new MessageCreateBuilder()
                    .setContent("Pick your president!")
                    .addActionRow(menuBuilder.build())
                    // TODO: remove debug button
                    .addActionRow(Button.of(ButtonStyle.PRIMARY, "proceedVotes" + i, "[DEBUG] Proceed votes"))
                    .build();

            MessageCreateAction sendMessage = getJDA().getVoiceChannelById(properties.get(i).voiceChatID())
                    .sendMessage(message);
            actions.add(sendMessage);
        }
        return RestAction.allOf(actions);
    }

    private RestAction<?> createChannelsAndRoles() {
        Guild guild = getJDA().getGuildById(session.getIODevice().getGuildId());
        Category category = guild.createCategory(Constants.bundle.getString("game_name")).complete();
        List<Role> roles = createRoles(guild).complete();

        List<RestAction<?>> actions = new ArrayList<>(phaseLogic.getTeamCount());

        for (int i = 0; i < phaseLogic.getTeamCount(); i++) {
            Role role = roles.get(i);
            final int index = i;
            final long roleId = role.getIdLong();
            TeamBuilder builder = phaseLogic.getTeamBuilder(i);
            RestAction<List<Void>> addMembersToRole = addMembersToRole(role, builder.getMembers());
            RestAction<VoiceChannel> createVC = createVoiceTeamChannel(category, role,
                    builder.getDescription().getName())
                            .onSuccess(vc -> properties.set(index, new DiscordTeamProperty(vc.getIdLong(), roleId)));
            actions.add(addMembersToRole);
            actions.add(createVC);
        }

        return RestAction.allOf(actions);
    }

    private RestAction<List<Role>> createRoles(Guild guild) {
        List<RoleAction> actions = new ArrayList<>(phaseLogic.getTeamCount());
        for (int i = 0; i < phaseLogic.getTeamCount(); i++) {
            TeamBuilder builder = phaseLogic.getTeamBuilder(i);
            RoleAction action = guild.createRole()
                    .setName(builder.getDescription().getName())
                    .setColor(builder.getDescription().getColor())
                    .setMentionable(true);

            actions.add(action);
        }
        return RestAction.allOf(actions);
    }

    private RestAction<List<Void>> addMembersToRole(Role role, Set<? extends IMember> members) {
        List<AuditableRestAction<Void>> actions = new ArrayList<>(members.size());
        for (IMember m : members) {
            AuditableRestAction<Void> action = role.getGuild()
                    .addRoleToMember(getJDA().getUserById(m.getID()), role)
                    .reason("Added role by World Domination Game");
            actions.add(action);
        }
        return RestAction.allOf(actions);
    }

    private ChannelAction<VoiceChannel> createVoiceTeamChannel(Category category, Role role, String name) {
        Guild guild = category.getGuild();
        return category.createVoiceChannel(name)
                .addRolePermissionOverride(
                        guild.getPublicRole().getIdLong(),
                        0,
                        Permission.VOICE_CONNECT.getRawValue())
                .addRolePermissionOverride(
                        role.getIdLong(),
                        Permission.VOICE_CONNECT.getRawValue(),
                        0);
    }

    @Override
    public void onStringSelectInteraction(StringSelectInteractionEvent event) {
        long voter = event.getUser().getIdLong();
        long voted = Long.parseLong(event.getValues().get(0));
        phaseLogic.vote(new DiscordMember(voter), new DiscordMember(voted));
        event.deferEdit().queue();
        System.out.println("[" + voter + "] -> " + "[" + voted + "]");
    }

    @Override
    public int getDurationInMilliseconds() { return 1000 * 60 * 3; }

    @Override
    public void phaseEnding() {
        phaseLogic.proceedVotes();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < phaseLogic.getTeamCount(); i++) {
            TeamBuilder t = phaseLogic.getTeamBuilder(i);
            sb.append(t.getDescription().getFullName()).append(": ");
            sb.append(getJDA().getUserById(t.getPresident().getID()).getName()).append("\n");
        }

        getJDA().getTextChannelById(1125882793331785783L).sendMessage(sb.toString()).queue();
        System.out.println("Ended pres-picking.");
    }

    // TODO: remove debug button
    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        event.deferEdit().queue();
        cancelTimer();
        phaseEnding();
    }

    public void nextPhase() {
        System.out.println("Next phase");
        /* session.setPhase(new TalkingPhase(session)); */
    }

}
