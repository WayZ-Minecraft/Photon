package niwer.queryon.queries.interaction;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import niwer.queryon.DataBase;
import niwer.queryon.queries.Expression;

public final class UpdateManager {

    private static UpdateCall lastCall;
    private static RuntimeException nextFailure;

    private final Class<?> tableClass;
    private final Map<String, Object> values = new LinkedHashMap<>();
    private final List<String> whereClauses = new ArrayList<>();

    private UpdateManager(Class<?> tableClass) {
        this.tableClass = tableClass;
    }

    public static void reset() {
        lastCall = null;
        nextFailure = null;
    }

    public static void setNextFailure(RuntimeException failure) { nextFailure = failure; }
    public static UpdateCall lastCall() { return lastCall; }

    public static UpdateManager update(DataBase db, Class<?> tableClass) {
        return new UpdateManager(tableClass);
    }

    public UpdateManager set(String column, Object value) {
        this.values.put(column, value);
        return this;
    }

    public UpdateManager where(Expression expression) {
        this.whereClauses.add(String.valueOf(expression));
        return this;
    }

    public void execute() {
        if (nextFailure != null) throw nextFailure;
        lastCall = new UpdateCall(this.tableClass, new LinkedHashMap<>(this.values), new ArrayList<>(this.whereClauses));
    }

    public record UpdateCall(Class<?> tableClass, Map<String, Object> values, List<String> whereClauses) {}
}