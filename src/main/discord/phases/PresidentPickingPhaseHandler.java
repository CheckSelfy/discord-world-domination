package discord.phases;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import discord.DiscordIODevice;
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

    public PresidentPickingPhaseHandler(Session<DiscordIODevice, IDiscordPhaseEventHandler> session,
            ArrayList<TeamBuilder> builders) {
        super(session);

        this.phaseLogic = new PresidentPickingPhaseLogic(this, builders);

        createChannelsAndRoles();
    }

    private void createChannelsAndRoles() {
        Guild guild = getJDA().getGuildById(session.getIODevice().getGuildId());
        guild.createCategory(Constants.bundle.getString("game_name")).and(
                createRoles(guild),
                (Category category, List<Role> roles) -> {
                    List<RestAction<?>> actions = new ArrayList<>(2 * roles.size());

                    for (int i = 0; i < phaseLogic.getTeamCount(); i++) {
                        Role role = roles.get(i);
                        TeamBuilder builder = phaseLogic.getTeamBuilder(i);
                        RestAction<List<Void>> givingToUser = addMembersToRole(role, builder.getMembers());
                        ChannelAction<VoiceChannel> createVoice = createVoiceTeamChannel(category, role, builder.getDescription().getName());
                        actions.add(givingToUser);
                        actions.add(createVoice);
                    }
                    return RestAction.allOf(actions);
                }).flatMap(a -> a).queue(); // oh my god, can you help me to fix this stupid looking code...
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
