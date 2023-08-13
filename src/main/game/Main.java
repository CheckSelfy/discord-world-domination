package game;

import game.orders.*;
import game.entities.*;

public class Main {
    static final Country green = new Country("green", new City[] { new City("g-city1", 10), new City("g-city2", 20),
            new City("g-city3", 30), new City("g-city4", 40) });

    static final Country yellow = new Country("yellow", new City[] { new City("y-city1", 11), new City("y-city2", 21),
            new City("y-city3", 31), new City("y-city4", 41) });

    static final Country[] countries = { green, yellow };

    public static void main(String[] args) {
        Game game = new Game(countries);

        System.out.println("countreis:");
        for (Country country : game.getCountries()) {
            System.out.println(country);
        }

        Order order = createOrder();
        game.acceptOrder(order);

        System.out.println("countreis:");
        for (Country country : game.getCountries()) {
            System.out.println(country);
        }
    }

    static Order createOrder() {
        var orderBuilder = new OrderBuilder();
        for (int i = 0; i < 4; i++) {
            orderBuilder.addAction(new ShieldAction(yellow, i));
        }
        orderBuilder.addAction(new MoneyTransferAction(yellow, green, 100));
        return orderBuilder.build();
    }
}
