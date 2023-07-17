import java.util.Map;
import java.util.Set;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

public class GameCommunicator extends ListenerAdapter {
    final long guildId;
    final Game game;
    final Map<String, Set<Long>> teams;

    public GameCommunicator(long guildId, Map<String, Set<Long>> teams, JDA jda) {
        this.guildId = guildId;
        this.teams = teams;
        this.game = new Game();

        Category category = jda.getGuildById(guildId).createCategory("Global domination").complete();

        for (Map.Entry<String, Set<Long>> team: teams.entrySet()) {
            if (team.getValue().isEmpty())
                continue;

            Role role = jda
                .getGuildById(guildId)
                .createRole()
                .setName(Constants.bundle.getString(team.getKey()))
                .setColor(Integer.parseInt(Constants.bundle.getString(team.getKey() + "_color"), 16))
                .complete();

            for (long userId: team.getValue()) {
                category.getGuild().addRoleToMember(User.fromId(userId), role).queue();
            }

            VoiceChannel voiceCh = category
                .createVoiceChannel(Constants.bundle.getString(team.getKey()))
                .addRolePermissionOverride(
                    jda.getGuildById(guildId).getPublicRole().getIdLong(), 
                    0,
                    Permission.VOICE_CONNECT.getRawValue()
                    )
                .addRolePermissionOverride(
                    role.getIdLong(), 
                    Permission.VOICE_CONNECT.getRawValue(), 
                    0)
                .complete();
            
            MessageCreateData data = 
                new MessageCreateBuilder()
                    .setContent("Possible actions")
                    .setComponents(ActionRow.of(Button.of(ButtonStyle.SUCCESS, "moveToOther", "Move to other", Emoji.fromUnicode(Constants.bundle.getString("random_emoji")))))
                    .build();

            Message msg = voiceCh.sendMessage(data).complete();

            msg.getJDA().addEventListener(new MoveToOther(msg.getIdLong()));
        }
    }


    public class MoveToOther extends ListenerAdapter {
        long messageId;

        public MoveToOther(long messageId) {
            this.messageId = messageId;
        }

        @Override
        public void onButtonInteraction(ButtonInteractionEvent event) {
            if (event.getMessageIdLong() != messageId) 
                return;

            if (event.getButton().getId() != "moveToOther")
                return;
            
                // YOU STAYED HERE
        }
    }

}