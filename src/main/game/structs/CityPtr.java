package game.structs;

import game.entities.Country;

public record CityPtr(Country country, int cityIndex) {}
