package game.entities;

public class Member {
    private final long userId;
    // private boolean isPresident;
    private Team team;

    public Member(long userId) { this(userId, null); }

    public Member(long userId, Team team) {
        this.userId = userId;
        // isPresident = false;
        this.team = team;
    }

    public long getUserId() { return userId; }

    // public boolean isPresident() { return isPresident; }

    // public void setPresident(boolean isPresident) { this.isPresident = isPresident; }

    public Team getTeam() { return team; }

    public void setTeam(Team team) { this.team = team; }
}
