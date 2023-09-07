package discord.phases;

import java.util.concurrent.TimeUnit;

import discord.DiscordIODevice;
import discord.entities.DiscordTeam;
import game.Game;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import social_logic.Session;
import social_logic.entities.IMember;
import social_logic.phases.handlers_interfaces.ISummitPhaseEventHandler;

public class SummitPhaseHandler extends ADiscordPhaseEventHandler
        implements ISummitPhaseEventHandler {
    private final Game<DiscordTeam> game;
    private final int talkDuration = 1000 * 5;
    private final long voiceChannelId;

    public SummitPhaseHandler(Session<DiscordIODevice, IDiscordPhaseEventHandler> session, Game<DiscordTeam> game) {
        super(session);
        this.game = game;

        // create summit channel
        Category category = getJDA().getVoiceChannelById(game.getCountries().get(0).getProperty().voiceChatId())
                .getParentCategory();
        VoiceChannel voiceChannel = category.createVoiceChannel("Summit").complete();
        voiceChannelId = voiceChannel.getIdLong();

        // move all users to one channel
        Guild guild = category.getGuild();
        for (int i = 0; i < game.getCountries().size(); i++) {
            DiscordTeam team = game.getCountries().get(i);

            for (IMember teamMember : team.getMembers()) {
                Member member = guild.getMemberById(teamMember.getId());
                try {
                    guild.moveVoiceMember(member, voiceChannel).queue();
                } catch (Exception ignored) {
                }
            }

            schedule(() -> {
                voiceChannel.sendMessage(talkDuration / 1000 + " second for " + team.getDescription().getFullName())
                        .queue();
                muteAllExcept(guild, team);
            }, talkDuration * i);
        }

        schedule(() -> {
            voiceChannel
                    .sendMessage((getDurationInMilliseconds() - talkDuration * game.getCountries().size()) / 1000
                            + " second for All teams")
                    .queue();
            setMuteAll(guild, false);
        }, talkDuration * game.getCountries().size());
        scheduleEnd();
    }

    private void setMuteAll(Guild guild, boolean muted) {
        for (DiscordTeam team : game.getCountries()) {
            setMuteTeam(guild, team, muted);
        }
    }

    private void muteAllExcept(Guild guild, DiscordTeam exceptTeam) {
        for (DiscordTeam team : game.getCountries()) {
            setMuteTeam(guild, team, team != exceptTeam);
        }
    }

    private void setMuteTeam(Guild guild, DiscordTeam team, boolean muted) {
        for (IMember teamMember : team.getMembers()) {
            try {
                guild.mute(guild.getMemberById(teamMember.getId()), muted).queue();
            } catch (Exception ignored) {
            }
        }
    }

    @Override
    public void phaseEnding() {
        Guild guild = getJDA().getGuildById(session.getIODevice().getGuildId());
        setMuteAll(guild, false);
        guild.getChannelById(VoiceChannel.class, voiceChannelId).delete().queueAfter(talkDuration,
                TimeUnit.MILLISECONDS);
        nextPhase();
    }

    @Override
    public int getDurationInMilliseconds() { return talkDuration * 4; }

    @Override
    public void nextPhase() {
        System.out.println("Next phase: TalkingPhase");
        session.setPhaseHandler(new TalkingPhaseHandler(session, game));
    }

}
