import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.MessageEmbed.Field;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.react.GenericMessageReactionEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;
import net.dv8tion.jda.api.utils.messages.MessageEditData;

public class GameCommunicator extends ListenerAdapter {
    final static Emoji wind = Emoji.fromUnicode("💨");

    final long guildId;
    final List<Team> teams;
    final Game game;

    public GameCommunicator(long guildId, List<Team> teams, JDA jda) {
        this.guildId = guildId;
        this.teams = teams;

        createVoiceChannelsAndRoles(jda);

        game = new Game();

        sendSendDelegateMessages(jda);
        sendReceiveDelegateMessages(jda);
    }

    private void createVoiceChannelsAndRoles(JDA jda) {
        Guild guild = jda.getGuildById(guildId);
        Category category = guild.createCategory("Global domination").complete();

        List<CompletableFuture<Void>> actions = new ArrayList<>(Constants.COUNTRIES_COUNT);

        for (Team team : teams) {
            Set<Long> users = team.getUsers();

            if (users.isEmpty())
                continue;

            actions.add(jda
                    .getGuildById(guildId)
                    .createRole()
                    .setName(team.getLocalization().getFullName())
                    .setColor(team.getLocalization().getColor())
                    .flatMap(
                            role -> {
                                for (Long userId : users)
                                    guild.addRoleToMember(User.fromId(userId), role).queue(); // set role to members

                                team.setRoleId(role.getIdLong()); // set role to team

                                return category
                                        .createVoiceChannel(team.getLocalization().getFullName())
                                        .addRolePermissionOverride( // forbid to connect others
                                                guild.getPublicRole().getIdLong(),
                                                0,
                                                Permission.VOICE_CONNECT.getRawValue())
                                        .addRolePermissionOverride( // allow to connect members
                                                role.getIdLong(),
                                                Permission.VOICE_CONNECT.getRawValue(),
                                                0);
                            })
                    .submit().thenAccept(vc -> team.setVoiceChannelId(vc.getIdLong())));
        }

        CompletableFuture.allOf(actions.toArray(new CompletableFuture[actions.size()])).join();
    }

    private void sendSendDelegateMessages(JDA jda) {
        MessageCreateData msgToSend = new MessageCreateBuilder()
                .setContent("Pick countries you want to send delegationi mission.").build();
        for (Team team : teams) {
            jda.getVoiceChannelById(team.getVoiceChannelId())
                    .sendMessage(msgToSend).flatMap(
                            msg -> {
                                team.setSendDelegationMessageId(msg.getIdLong());
                                return Team.putCountriesEmoji(msg, teams, team);
                            })
                    .queue();
        }
    }

    private void sendReceiveDelegateMessages(JDA jda) {
        MessageCreateData msgToSend = new MessageCreateBuilder()
                .setContent("There are countries that send requests to you!")
                .addEmbeds(new EmbedBuilder().addBlankField(true).build()).build();

        for (Team team : teams) {
            jda.getVoiceChannelById(team.getVoiceChannelId())
                    .sendMessage(msgToSend).flatMap(
                            msg -> {
                                team.setReceiveDelegationMessageId(msg.getIdLong());
                                return Team.putCountriesEmoji(msg, teams, team);
                            })
                    .queue();
        }
    }

    @Override
    public void onGenericMessageReaction(GenericMessageReactionEvent event) {
        if (event.getGuild().getIdLong() != guildId)
            return;

        if (event.getUser().isBot())
            return;

        if (Team.getTeamBySendDelegationMessage(teams, event.getChannel().getIdLong(),
                event.getMessageIdLong()) != null) {
            sendOrDeleteRequest(event);
        } else {
            approveRequest(event);
        }

    }

    private void approveRequest(GenericMessageReactionEvent event) {
        Team homeTeam = Team.getTeamByReceiveDelegationMessage(teams, event.getChannel().getIdLong(),
                event.getMessageIdLong());
        Team guestTeam = Team.getTeamByEmoji(teams, event.getEmoji());

        if (homeTeam == null || guestTeam == null || homeTeam == guestTeam) {
            return;
        }

        MessageCreateData msgToSend = new MessageCreateBuilder()
                .setContent("Team " + homeTeam.getLocalization().getFullName() + " waits your delegation!")
                .setComponents(ActionRow.of(
                        Button.of(ButtonStyle.SUCCESS,
                                "join_" +
                                        homeTeam.getLocalization().getName() +
                                        "_" +
                                        guestTeam.getLocalization().getName(),
                                "Join!",
                                Emoji.fromUnicode("🛞"))))
                .build();

        event.getJDA()
                .getVoiceChannelById(guestTeam.getVoiceChannelId())
                .sendMessage(msgToSend)
                .flatMap(msg -> msg.addReaction(wind))
                .queue();
    }

    private void sendOrDeleteRequest(GenericMessageReactionEvent event) {
        Team guestTeam = Team.getTeamBySendDelegationMessage(teams, event.getChannel().getIdLong(),
                event.getMessageIdLong());
        Team homeTeam = Team.getTeamByEmoji(teams, event.getEmoji());

        if (guestTeam == null || homeTeam == null || guestTeam == homeTeam) // we can't forbid users to use reaction of
                                                                            // their country
            return;

        if (event instanceof MessageReactionAddEvent) {
            homeTeam.addRequest(guestTeam);
        } else {
            homeTeam.removeRequest(guestTeam);
        }

        updateReceiveMessage(homeTeam, event.getJDA());
    }

    private void updateReceiveMessage(Team team, JDA jda) {
        EmbedBuilder builder = new EmbedBuilder()
                .setTitle("Requests to you");

        for (Team guest : team.getReceivedRequests()) {
            builder.addField(guest.getLocalization().getFullName(), "", false);
        }

        jda.getVoiceChannelById(team.getVoiceChannelId())
                .editMessageEmbedsById(team.getReceiveDelegationMessageId(), builder.build())
                .queue();

    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        String idString = event.getButton().getId(); // join_home_guest

        Message msg = event.getMessageChannel().retrieveMessageById(event.getMessageIdLong()).complete();

        String[] strs = idString.split("_");

        Team homeTeam = Team.getTeamByName(teams, strs[1]);
        Team guestTeam = Team.getTeamByName(teams, strs[2]);

        List<User> usersToMove = msg
            .getReaction(wind)
            .retrieveUsers()
            .complete();

        System.out.println("usersToMove = " + usersToMove);
        
        Guild guild = event.getJDA().getGuildById(guildId);

        for (User user: usersToMove) {
            guild.moveVoiceMember(
                guild.getMemberById(user.getIdLong()),
                guild.getVoiceChannelById(homeTeam.getVoiceChannelId())
                ).queue();
            }
    }
}