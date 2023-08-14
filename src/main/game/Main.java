package game;

import game.orders.*;
import game.orders.Order.IAction;
import game.entities.*;

public class Main {
    static final Country green = new Country("green", new City[] {
            new City("g-city1", 10),
            new City("g-city2", 20),
            new City("g-city3", 30),
            new City("g-city4", 40) });

    static final Country yellow = new Country("yellow", new City[] {
            new City("y-city1", 11),
            new City("y-city2", 21),
            new City("y-city3", 31),
            new City("y-city4", 41) });

    static final Country[] countries = { green, yellow };

    public static void main(String[] args) {
        try {
            Game game = new Game(countries);
            printStat(game);
            game.acceptOrder(createOrder(new DevelopNuclearAction(green)));
            printStat(game);
            game.acceptOrder(createOrder(new ShieldCityAction(yellow, 0), new UpgradeCityAction(yellow, 0),
                    new ImproveEcologyAction(yellow)));
            printStat(game);
            game.acceptOrder(createOrder(new BuildMissilesAction(green, 2)));
            printStat(game);
            City yCity1 = yellow.getCities()[0];
            game.acceptOrder(createOrder(new SendMissileAction(green, yCity1)));
            printStat(game);
            game.acceptOrder(createOrder(new SendMissileAction(green, yCity1)));
            printStat(game);
        } catch (Exception e) {
            System.err.println();
            System.err.println("==== ERROR!!! ====");
            System.err.println(e.getMessage());
            System.err.println();
        }
    }

    private static void printStat(Game game) {
        System.out.println(game);
        System.out.println();
    }

    static Order createOrder(IAction... actions) {
        var orderBuilder = new OrderBuilder();
        for (IAction action : actions) {
            orderBuilder.addAction(action);
        }
        return orderBuilder.build();
    }
}
