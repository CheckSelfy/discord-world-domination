package social_logic.entities;

@FunctionalInterface
public interface TeamBuilderFactory<TB extends TeamBuilder<T>, T extends Team> {
    public TB create();
}
