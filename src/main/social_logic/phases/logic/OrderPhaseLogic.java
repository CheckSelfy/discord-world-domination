package social_logic.phases.logic;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import game.Game;
import game.OrderBuilder;
import game.actions.IAction;
import social_logic.entities.Team;
import social_logic.phases.handlers_interfaces.IOrderPhaseEventHandler;

public class OrderPhaseLogic<T extends Team> {
    private final IOrderPhaseEventHandler handler;
    private final List<OrderBuilder> orderBuilders;
    private final Game<T> game;

    public OrderPhaseLogic(IOrderPhaseEventHandler handler, Game<T> game) {
        this.handler = handler;
        this.orderBuilders = new ArrayList<OrderBuilder>(game.getCountries().size());
        this.game = game;
    }

    public List<T> getTeams() { return game.getCountries(); }

    public Iterator<IAction> getIterator(int index) {
        return orderBuilders.get(index).iterator();
    }
}