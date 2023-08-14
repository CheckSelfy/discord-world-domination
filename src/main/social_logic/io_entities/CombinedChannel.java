package social_logic.io_entities;

public class CombinedChannel {
    ITextChannel textChannel;
    IVoiceChannel voiceChannel;

    public CombinedChannel(ITextChannel textChannel, IVoiceChannel voiceChannel) {
        this.textChannel = textChannel;
        this.voiceChannel = voiceChannel;
    }

    public ITextChannel getTextChannel() {
        return textChannel;
    }

    public IVoiceChannel getVoiceChannel() {
        return voiceChannel;
    }
}
