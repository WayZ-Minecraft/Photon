package niwer.photon.web.endpoints.admin;

import java.util.List;

import io.javalin.http.Context;
import niwer.photon.web.AdminSessionManager;
import niwer.photon.web.endpoints.IEndpoint;

public class AdminTablesEndpoint implements IEndpoint {

    private static final List<TableInfo> TABLES = List.of(
        new TableInfo("Server", "Servers", "Saved server status rows"),
        new TableInfo("PlayerAccount", "Accounts", "Registered player accounts"),
        new TableInfo("License", "Licenses", "Issued licenses"),
        new TableInfo("HWID", "HWIDs", "HWID bindings"),
        new TableInfo("CrashReport", "Crash reports", "Stored crash reports"),
        new TableInfo("Anticheat", "Anti-cheat reports", "Stored anti-cheat reports"),
        new TableInfo("DiscordLog", "Discord logs", "Moderation logs"),
        new TableInfo("DiscordAccount", "Discord profiles", "Discord progression profiles")
    );

    @Override public String path() { return "/api/admin/tables"; }

    @Override public HttpMethod method() { return HttpMethod.GET; }

    @Override
    public void handle(Context handler) {
        if (AdminSessionManager.requireAdministrator(handler) == null) return;
        handler.json(TABLES);
    }

    public static List<TableInfo> getTables() {
        return TABLES;
    }

    public record TableInfo(String table, String label, String description) {}
}