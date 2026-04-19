package niwer.queryon.queries.interaction;

import java.util.ArrayList;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;

import niwer.queryon.DataBase;
import niwer.queryon.queries.Expression;
import niwer.queryon.SQLSerializable;

public final class SelectionManager {

    public enum EnumOrder { ASC, DESC }

    private static SelectionCall lastCall;
    private static List<?> nextListResult = List.of();
    private static Object nextSerializableResult;
    private static Object nextPrimitiveResult;
    private static boolean nextHasResult;
    private static final Deque<Boolean> nextHasResultSequence = new ArrayDeque<>();
    private static RuntimeException nextFailure;

    private final Class<?> tableClass;
    private final List<String> columns;
    private final List<String> whereClauses = new ArrayList<>();
    private String orderByColumn;
    private EnumOrder order;
    private Integer limit;
    private String terminalMethod;

    private SelectionManager(Class<?> tableClass, String... columns) {
        this.tableClass = tableClass;
        this.columns = Arrays.asList(columns);
    }

    public static void reset() {
        lastCall = null;
        nextListResult = List.of();
        nextSerializableResult = null;
        nextPrimitiveResult = null;
        nextHasResult = false;
        nextHasResultSequence.clear();
        nextFailure = null;
    }

    public static void setNextListResult(List<?> result) { nextListResult = result; }
    public static void setNextSerializableResult(Object result) { nextSerializableResult = result; }
    public static void setNextPrimitiveResult(Object result) { nextPrimitiveResult = result; }
    public static void setNextHasResult(boolean result) { nextHasResult = result; }
    public static void setNextHasResultSequence(boolean... results) {
        nextHasResultSequence.clear();
        for (boolean result : results) nextHasResultSequence.addLast(result);
    }
    public static void setNextFailure(RuntimeException failure) { nextFailure = failure; }

    public static SelectionCall lastCall() { return lastCall; }

    public static SelectionManager select(DataBase db, Class<?> tableClass, String... columns) {
        return new SelectionManager(tableClass, columns);
    }

    public static SelectionManager select(DataBase db, Class<?> tableClass) {
        return new SelectionManager(tableClass);
    }

    public SelectionManager where(Expression expression) {
        this.whereClauses.add(String.valueOf(expression));
        return this;
    }

    public SelectionManager orderBy(String column, EnumOrder order) {
        this.orderByColumn = column;
        this.order = order;
        return this;
    }

    public SelectionManager limit(int value) {
        this.limit = value;
        return this;
    }

    public <T> List<T> executeList(Class<T> type) {
        finish("executeList");
        @SuppressWarnings("unchecked")
        final List<T> result = (List<T>) nextListResult;
        return result == null ? new ArrayList<>() : result;
    }

    public <T> SQLSerializable<T> executeSerializable(Class<T> type) {
        finish("executeSerializable");
        @SuppressWarnings("unchecked")
        final SQLSerializable<T> result = (SQLSerializable<T>) nextSerializableResult;
        return result;
    }

    public <T> T executePrimitive(Class<T> type) {
        finish("executePrimitive");
        @SuppressWarnings("unchecked")
        final T result = (T) nextPrimitiveResult;
        return result;
    }

    public boolean executeHasResult() {
        finish("executeHasResult");
        return nextHasResultSequence.isEmpty() ? nextHasResult : nextHasResultSequence.removeFirst();
    }

    private void finish(String terminalMethod) {
        this.terminalMethod = terminalMethod;
        if (nextFailure != null) throw nextFailure;
        lastCall = new SelectionCall(this.tableClass, this.columns, new ArrayList<>(this.whereClauses), this.orderByColumn, this.order, this.limit, terminalMethod);
    }

    public record SelectionCall(Class<?> tableClass, List<String> columns, List<String> whereClauses, String orderByColumn, EnumOrder order, Integer limit, String terminalMethod) {}
}