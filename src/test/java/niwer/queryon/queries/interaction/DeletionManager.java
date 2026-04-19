package niwer.queryon.queries.interaction;

import java.util.ArrayList;
import java.util.List;

import niwer.queryon.DataBase;
import niwer.queryon.queries.Expression;

public final class DeletionManager {

    private static DeletionCall lastCall;
    private static RuntimeException nextFailure;

    private final Class<?> tableClass;
    private final List<String> whereClauses = new ArrayList<>();

    private DeletionManager(Class<?> tableClass) {
        this.tableClass = tableClass;
    }

    public static void reset() {
        lastCall = null;
        nextFailure = null;
    }

    public static void setNextFailure(RuntimeException failure) { nextFailure = failure; }
    public static DeletionCall lastCall() { return lastCall; }

    public static DeletionManager delete(DataBase db, Class<?> tableClass) {
        return new DeletionManager(tableClass);
    }

    public DeletionManager where(Expression expression) {
        this.whereClauses.add(String.valueOf(expression));
        return this;
    }

    public void execute() {
        if (nextFailure != null) throw nextFailure;
        lastCall = new DeletionCall(this.tableClass, new ArrayList<>(this.whereClauses));
    }

    public record DeletionCall(Class<?> tableClass, List<String> whereClauses) {}
}