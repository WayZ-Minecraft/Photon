package niwer.queryon.queries.interaction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import niwer.queryon.DataBase;

public final class InsertionManager {

    private static InsertionCall lastCall;
    private static RuntimeException nextFailure;

    private final boolean insertOrIgnore;
    private final Class<?> tableClass;
    private final List<String> columns;
    private final List<Object[]> rows = new ArrayList<>();

    private InsertionManager(boolean insertOrIgnore, Class<?> tableClass, String... columns) {
        this.insertOrIgnore = insertOrIgnore;
        this.tableClass = tableClass;
        this.columns = Arrays.asList(columns);
    }

    public static void reset() {
        lastCall = null;
        nextFailure = null;
    }

    public static void setNextFailure(RuntimeException failure) {
        nextFailure = failure;
    }

    public static InsertionCall lastCall() {
        return lastCall;
    }

    public static InsertionManager insert(DataBase db, Class<?> tableClass, String... columns) {
        return new InsertionManager(false, tableClass, columns);
    }

    public static InsertionManager insertOrIgnore(DataBase db, Class<?> tableClass, String... columns) {
        return new InsertionManager(true, tableClass, columns);
    }

    public InsertionManager row(Object... values) {
        this.rows.add(values);
        return this;
    }

    public void execute() {
        if (nextFailure != null) throw nextFailure;
        lastCall = new InsertionCall(this.insertOrIgnore, this.tableClass, this.columns, new ArrayList<>(this.rows));
    }

    public record InsertionCall(boolean insertOrIgnore, Class<?> tableClass, List<String> columns, List<Object[]> rows) {}
}