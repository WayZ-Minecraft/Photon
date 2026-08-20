package niwer.photon.web.endpoints.admin;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.javalin.http.Context;
import niwer.photon.PhotonEngine;
import niwer.photon.util.session.AdminSessionManager;
import niwer.photon.web.HttpMethod;
import niwer.photon.web.endpoints.IEndpoint;

public class AdminTableDataEndpoint implements IEndpoint {

    @Override public String path() { return "/api/admin/tables/{table}"; }

    @Override public HttpMethod method() { return HttpMethod.GET; }

    @Override
    public void handle(Context handler) {
        IEndpoint.setupRateLimit(handler, 5, TimeUnit.MINUTES);

        if (AdminSessionManager.requireAdministrator(handler) == null) return;

        final String tableName = handler.pathParam("table");
        final AdminTablesEndpoint.TableInfo tableInfo = AdminTablesEndpoint.getTables().stream()
            .filter(info -> info.table().equalsIgnoreCase(tableName))
            .findFirst()
            .orElse(null);

        if (tableInfo == null) {
            handler.status(404).result("Unknown table");
            return;
        }

        final int limit = clampLimit(handler.queryParam("limit"));
        final List<String> columns = new ArrayList<>();
        final List<Map<String, Object>> rows = new ArrayList<>();

        try {
            PhotonEngine.DATA_BASE.connect();
            try (Statement statement = PhotonEngine.DATA_BASE.sqlConnection().createStatement();
                 ResultSet result = statement.executeQuery("SELECT * FROM \"" + tableInfo.table() + "\" LIMIT " + limit)) {

                final ResultSetMetaData metadata = result.getMetaData();
                for (int column = 1; column <= metadata.getColumnCount(); column++) {
                    columns.add(metadata.getColumnName(column));
                }

                while (result.next()) {
                    final Map<String, Object> row = new LinkedHashMap<>();
                    for (int column = 1; column <= metadata.getColumnCount(); column++) {
                        row.put(metadata.getColumnName(column), normalizeValue(result.getObject(column)));
                    }
                    rows.add(row);
                }
            }
        } catch (Exception e) {
            handler.status(500).result("Failed to load table: " + e.getMessage());
            return;
        }

        handler.json(new TableData(tableInfo.table(), tableInfo.label(), columns, rows));
    }

    private static int clampLimit(String limitValue) {
        final int defaultLimit = 250;
        if (limitValue == null || limitValue.isBlank()) return defaultLimit;

        try {
            return Math.max(1, Math.min(Integer.parseInt(limitValue), 500));
        } catch (NumberFormatException e) {
            return defaultLimit;
        }
    }

    private static Object normalizeValue(Object value) {
        if (value instanceof java.util.Date date) return date.toString();
        return value;
    }

    public record TableData(String table, String label, List<String> columns, List<Map<String, Object>> rows) {}
}