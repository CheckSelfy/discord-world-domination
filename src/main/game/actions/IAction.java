package game.actions;

import game.Game;
import game.entities.Country;

public interface IAction {
    public void doAction(Game<? extends Country> game);

    public int price();

    public boolean missileRequired();

    public boolean requreNuclear();

    public Country getCountry();
}