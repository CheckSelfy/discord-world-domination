package discord.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import discord.entities.DiscordTeamBuilder;
import discord.entities.DiscordTeamProperty;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
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
import social_logic.entities.IMember;
import util.Constants;

public class ServerSetupUtil {
    private final JDA jda;
    private final List<DiscordTeamBuilder> teamBuilders;

    private static final String pickPresident = "votePresident";

    public ServerSetupUtil(JDA jda, List<DiscordTeamBuilder> teamBuilders) {
        this.jda = jda;
        this.teamBuilders = teamBuilders;
    }

    public RestAction<?> createChannelsAndRoles(long guildId) {
        Guild guild = jda.getGuildById(guildId);
        Category category = guild.createCategory(Constants.bundle.getString("game_name")).complete();
        List<Role> roles = createRoles(guild).complete();

        List<RestAction<?>> actions = new ArrayList<>(teamBuilders.size());

        for (int i = 0; i < teamBuilders.size(); i++) {
            Role role = roles.get(i);
            final int index = i;
            final long roleId = role.getIdLong();
            DiscordTeamBuilder builder = teamBuilders.get(i);
            RestAction<List<Void>> addMembersToRole = addMembersToRole(role, builder.getMembers());
            RestAction<VoiceChannel> createVC = createVoiceTeamChannel(category, role,
                    builder.getDescription().getName())
                            .onSuccess(vc -> teamBuilders.get(index)
                                    .setProperty(new DiscordTeamProperty(vc.getIdLong(), roleId)));
            actions.add(addMembersToRole);
            actions.add(createVC);
        }

        return RestAction.allOf(actions);
    }

    public RestAction<List<Role>> createRoles(Guild guild) {
        List<RoleAction> actions = new ArrayList<>(teamBuilders.size());
        for (int i = 0; i < teamBuilders.size(); i++) {
            DiscordTeamBuilder builder = teamBuilders.get(i);
            RoleAction action = guild.createRole()
                    .setName(builder.getDescription().getName())
                    .setColor(builder.getDescription().getColor())
                    .setMentionable(true);

            actions.add(action);
        }
        return RestAction.allOf(actions);
    }

    public RestAction<List<Void>> addMembersToRole(Role role, Set<? extends IMember> members) {
        List<AuditableRestAction<Void>> actions = new ArrayList<>(members.size());
        for (IMember m : members) {
            AuditableRestAction<Void> action = role.getGuild()
                    .addRoleToMember(jda.getUserById(m.getID()), role)
                    .reason("Added role by World Domination Game");
            actions.add(action);
        }
        return RestAction.allOf(actions);
    }

    public ChannelAction<VoiceChannel> createVoiceTeamChannel(Category category, Role role, String name) {
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

    public RestAction<?> sendPolls() {
        List<MessageCreateAction> actions = new ArrayList<>(teamBuilders.size());
        for (int i = 0; i < teamBuilders.size(); i++) {
            Set<IMember> members = teamBuilders.get(i).getMembers();
            Builder menuBuilder = StringSelectMenu.create(pickPresident + i); // id of interaction
            for (IMember m : members) {
                User user = jda.getUserById(m.getID());
                menuBuilder.addOption(user.getName(), user.getId());
            }

            MessageCreateData message = new MessageCreateBuilder()
                    .setContent("Pick your president!")
                    .addActionRow(menuBuilder.build())
                    // TODO: remove debug button
                    .addActionRow(Button.of(ButtonStyle.PRIMARY, "proceedVotes" + i, "[DEBUG] Proceed votes"))
                    .build();

            MessageCreateAction sendMessage = jda.getVoiceChannelById(teamBuilders.get(i).getProperty().voiceChatID())
                    .sendMessage(message);
            actions.add(sendMessage);
        }
        return RestAction.allOf(actions);
    }

}
