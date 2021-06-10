package bool;

public interface BooleanExpression {
    

    // Methods
    public BooleanExpression evalWithCofactor(String varName, boolean value);
    public boolean getValue();
    @Override
    public String toString();
    @Override
    public boolean equals(Object o);
}
