package social_logic.entities;

@FunctionalInterface
public interface TeamBuilderFactory<TB extends TeamBuilder> {
    public TB create();
}
