package game.orders;

import game.Game;
import game.entities.Country;

public class Order {
    private IAction[] actions;

    public Order(IAction[] actions) { this.actions = actions; }

    public IAction[] getActions() { return actions; }

    public interface IAction {
        public void doAction(Game game);

        public int price();

        public boolean missileRequired();

        public boolean requreNuclear();

        public Country getCountry();
    }

    public static abstract class Action implements IAction {
        private final Country country;

        public Action(Country country) { this.country = country; }

        @Override
        public boolean missileRequired() { return false; }

        @Override
        public boolean requreNuclear() { return false; }

        public Country getCountry() { return country; }
    }
}
