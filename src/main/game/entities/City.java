package game.entities;

public class City {
    private int lifeLevel;
    private boolean isShielded = false;
    private boolean isAlive = true;
    private final String name;

    public City(final String name, int lifeLevel) {
        this.name = name;
        this.lifeLevel = lifeLevel;
    }

    public int getLifeLevel() { return lifeLevel; }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (isShielded()) {
            sb.append('{');
        }
        sb.append('.');
        sb.append(name);
        sb.append(": ");
        sb.append(isAlive() ? lifeLevel + "%" : "XXX");
        if (isShielded()) {
            sb.append('}');
        }
        return sb.toString();
    }

    public boolean isShielded() { return isShielded; }

    public void setShield() { this.isShielded = true; }

    public boolean isAlive() { return isAlive; }

    public void acceptMissile() {
        if (isShielded()) {
            isShielded = false;
        } else {
            isAlive = false;
            lifeLevel = 0;
        }
    }

    public void upgrade(int value) { lifeLevel += value; }

}
