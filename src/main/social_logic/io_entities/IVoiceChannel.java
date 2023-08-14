package social_logic.io_entities;

import java.util.Collection;

public interface IVoiceChannel {
    public void moveUser(IUser user);

    public default void moveUsers(Collection<? extends IUser> users) {
        for (IUser user : users) {
            moveUser(user);
        }
    }
}
