package niwer.photon.web.endpoints.accounts;

import io.javalin.http.Context;
import niwer.photon.Directories;
import niwer.photon.objects.ObjectPlayerAccount;
import niwer.photon.sql.PlayerAccountTable;
import niwer.photon.web.endpoints.IEndpoint;

public class UpdateProfileEndpoint implements IEndpoint {

    @Override public String path() { return "/accounts/update_profile"; }

    @Override public HttpMethod method() { return HttpMethod.POST; }

    @Override
    public void handle(Context handler) {
        final ProfileUpdateRequest request;
        try {
            request = Directories.GSON.fromJson(handler.body(), ProfileUpdateRequest.class);
        } catch (Exception exception) {
            handler.status(400).result("Invalid profile payload");
            return;
        }

        if (request == null || request.uuid == null || request.currentPassword == null) {
            handler.status(400).result("Missing parameters");
            return;
        }

        if (request.uuid.isBlank() || request.currentPassword.isBlank()) {
            handler.status(400).result("Parameters cannot be blank");
            return;
        }

        final ObjectPlayerAccount account = PlayerAccountTable.getAccountByUUID(request.uuid);
        if (account == null) {
            handler.status(404).result("Account not found");
            return;
        }

        if (account.password() == null || !account.password().equals(request.currentPassword)) {
            handler.status(401).result("Incorrect password");
            return;
        }

        final String nextUsername = request.username != null ? request.username.trim() : account.username();
        final String nextEmail = request.email != null ? request.email.trim().toLowerCase() : account.email();
        final String nextPassword = request.newPassword != null ? request.newPassword : null;

        if (nextUsername.isBlank() || nextEmail.isBlank()) {
            handler.status(400).result("Username and email cannot be blank");
            return;
        }

        if (!nextUsername.equalsIgnoreCase(account.username())) {
            final ObjectPlayerAccount existingUsername = PlayerAccountTable.getAccountByUsername(nextUsername);
            if (existingUsername != null && !existingUsername.uuid().equals(account.uuid())) {
                handler.status(400).result("An account with this username already exists.");
                return;
            }
            PlayerAccountTable.setUsername(account.uuid(), nextUsername);
        }

        if (!nextEmail.equalsIgnoreCase(account.email())) {
            final ObjectPlayerAccount existingEmail = PlayerAccountTable.getAccountByEmail(nextEmail);
            if (existingEmail != null && !existingEmail.uuid().equals(account.uuid())) {
                handler.status(400).result("An account with this email already exists.");
                return;
            }
            PlayerAccountTable.setEmail(account.uuid(), nextEmail);
        }

        if (nextPassword != null && !nextPassword.isBlank()) {
            if (nextPassword.length() < 8) {
                handler.status(400).result("Password must be at least 8 characters long");
                return;
            }
            if (request.confirmPassword == null || !nextPassword.equals(request.confirmPassword)) {
                handler.status(400).result("Passwords do not match");
                return;
            }
            PlayerAccountTable.setPassword(account.uuid(), nextPassword);
        }

        final ObjectPlayerAccount updatedAccount = PlayerAccountTable.getAccountByUUID(account.uuid());
        handler.json(updatedAccount == null ? account.toPublicMap() : updatedAccount.toPublicMap());
    }

    private record ProfileUpdateRequest(
        String uuid,
        String currentPassword,
        String username,
        String email,
        String newPassword,
        String confirmPassword
    ) {}
}
