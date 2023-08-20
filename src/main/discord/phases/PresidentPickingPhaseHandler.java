package discord.phases;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import discord.DiscordIODevice;
import discord.entities.DiscordTeamProperty;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.react.GenericMessageReactionEvent;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.AuditableRestAction;
import net.dv8tion.jda.api.requests.restaction.ChannelAction;
import net.dv8tion.jda.api.requests.restaction.RoleAction;
import social_logic.Session;
import social_logic.entities.IMember;
import social_logic.entities.TeamBuilder;
import social_logic.phases.handlers_interfaces.IPresidentPickingPhaseEventHandler;
import social_logic.phases.logic.PresidentPickingPhaseLogic;
import util.Constants;

// TODO: make PresidentPickingPhase
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

        for (DiscordTeamProperty pr: properties) {
            System.out.format("roleId: %d, channelId: %d\n", pr.roleID(), pr.voiceChatID());
        }
    }

    private RestAction<?> createChannelsAndRoles() {
        Guild guild = getJDA().getGuildById(session.getIODevice().getGuildId());
        Category category = guild.createCategory(Constants.bundle.getString("game_name")).complete();
        List<Role> roles = createRoles(guild).complete();

        List<RestAction<?>> actions = new ArrayList<>(phaseLogic.getTeamCount());

        for (int i = 0; i < phaseLogic.getTeamCount(); i++) {
            Role role = roles.get(i);
            final int index = i;                    // │ TODO
            final long roleId = role.getIdLong();   // └───────> how to get rid of these ones?
            TeamBuilder builder = phaseLogic.getTeamBuilder(i);
            RestAction<List<Void>> addMembersToRole = addMembersToRole(role, builder.getMembers());
            RestAction<VoiceChannel> createVC = createVoiceTeamChannel(category, role, builder.getDescription().getName())
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

    public void nextPhase() { /* session.setPhase(new TalkingPhase(session)); */ }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) { // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'onButtonInteraction'");
    }

    @Override
    public void onGenericMessageReaction(GenericMessageReactionEvent event) { // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'onGenericMessageReaction'");
    }
}
