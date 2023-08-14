package socialLogic.IOEntities;

public abstract class ACombinedChannel {
    ITextChannel textChannel;
    IVoiceChannel voiceChannel;

    public ACombinedChannel(ITextChannel textChannel, IVoiceChannel voiceChannel) {
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
