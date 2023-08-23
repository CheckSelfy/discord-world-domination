package game;

import java.util.List;

import game.entities.Country;

public class Order<C extends Country> {
    private List<IAction<C>> actions;

    public Order(List<IAction<C>> actions) { this.actions = actions; }

    public List<IAction<C>> getActions() { return actions; }

    public interface IAction<C extends Country> {
        public void doAction(Game<C> game);

        public int price();

        public boolean missileRequired();

        public boolean requreNuclear();

        public Country getCountry();
    }

    public static abstract class Action<C extends Country> implements IAction<C> {
        private final C country;

        public Action(C country) { this.country = country; }

        @Override
        public boolean missileRequired() { return false; }

        @Override
        public boolean requreNuclear() { return false; }

        public C getCountry() { return country; }

        protected void pay() { country.pay(price()); }
    }
}
