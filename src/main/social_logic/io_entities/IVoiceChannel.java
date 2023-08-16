package social_logic.io_entities;

public interface IVoiceChannel {
    public void moveUser(IUser user);

    public default void moveUsers(Iterable<? extends IUser> users) {
        for (IUser user : users) {
            moveUser(user);
        }
    }

    public long getChannelId();
}
