public class City {
    String name;
    boolean isLive;
    boolean shield;
    int development;
    int standardOfLiving;
    int income;

    public boolean isShielded() {
        return shield;
    }

    public void setShield(boolean shield) {
        this.shield = shield;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isLive() {
        return isLive;
    }

    public void setLive(boolean isLive) {
        this.isLive = isLive;
    }

    public int getDevelopment() {
        return development;
    }

    public void setDevelopment(int development) {
        this.development = development;
    }

    public int getStandardOfLiving() {
        return standardOfLiving;
    }

    public void setStandardOfLiving(int standardOfLiving) {
        this.standardOfLiving = standardOfLiving;
    }

    public int getIncome() {
        return income;
    }

    public void setIncome(int income) {
        this.income = income;
    }

    @Override
    public String toString() {
        return "City [name=" + name + ", isLive=" + isLive + ", development=" + development + ", standardOfLiving="
                + standardOfLiving + ", income=" + income + "]";
    }
}
