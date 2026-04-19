package niwer.queryon.queries;

public final class Expression {

    private final String expression;

    private Expression(String expression) {
        this.expression = expression;
    }

    public static Expression of(String value) {
        return new Expression(value);
    }

    public Expression isEqualTo(Object value) {
        return new Expression(this.expression + " = " + value);
    }

    public Expression and(Expression other) {
        return new Expression("(" + this.expression + ") AND (" + other.expression + ")");
    }

    public Expression isGreaterThanOrEqualTo(Object value) {
        return new Expression(this.expression + " >= " + value);
    }

    @Override
    public String toString() {
        return this.expression;
    }
}