package game.entities;

public class City {
    private int lifeLevel;
    private boolean isShielded;
    private boolean isAlive;
    private final String name;

    public City(final String name, int lifeLevel) {
        this.name = name;
        this.lifeLevel = lifeLevel;
        this.isShielded = false;
        this.isAlive = true;
    }

    public int getLifeLevel() { return lifeLevel; }

    @Override
    public String toString() { return (isShielded() ? "{." : ".") + name + (isShielded() ? "}" : ""); }

    public boolean isShielded() { return isShielded; }

    public void setShield() { this.isShielded = true; }
}
