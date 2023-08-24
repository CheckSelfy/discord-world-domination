package game;

import java.util.List;

import game.actions.IAction;

public class Order {
    private List<IAction> actions;

    public Order(List<IAction> actions) { this.actions = actions; }

    public List<IAction> getActions() { return actions; }
}
