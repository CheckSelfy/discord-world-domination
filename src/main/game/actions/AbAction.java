package game.actions;

import game.entities.Country;

public abstract class AbAction implements IAction {
    private final Country country;

    public AbAction(Country country) { this.country = country; }

    @Override
    public boolean missileRequired() { return false; }

    @Override
    public boolean requreNuclear() { return false; }

    public Country getCountry() { return country; }

    protected void pay() { country.pay(price()); }
}