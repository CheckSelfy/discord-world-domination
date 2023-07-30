package game.entities;

public class Member {
    long userId;
    boolean isPresident;

    public Member(long userId) {
        this.userId = userId;
        isPresident = false;
    }

    public long getUserId() {
        return userId;
    }

    public boolean isPresident() {
        return isPresident;
    }

    public void setPresident(boolean isPresident) {
        this.isPresident = isPresident;
    }
}
