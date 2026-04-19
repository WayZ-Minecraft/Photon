package niwer.photon.web.endpoints.accounts;

import niwer.photon.sql.PlayerAccountTable;
import niwer.photon.web.endpoints.IEndpoint;

import io.javalin.http.Context;

/**
 * Endpoint to handle account creation. This is a placeholder implementation and should be properly implemented with necessary validations, password hashing, and database storage.
 * 
 * @author Niwer
 */
public class CreateAccountEndpoint implements IEndpoint {

    @Override
    public String path() {
        return "/accounts/create_account";
    }

    @Override
    public HttpMethod method() {
        return HttpMethod.POST;
    }

    @Override
    public void handle(Context handler) {
        final String username = handler.formParam("username");
        final String email = handler.formParam("email");
        final String password = handler.formParam("password");

        /* Ensure all parameters are provided */
        if(username == null || email == null || password == null) {
            handler.status(400).result("Missing parameters");
            return;
        }

        /* Ensure no parameters are blank */
        if(username.isBlank() || email.isBlank() || password.isBlank()) {
            handler.status(400).result("Parameters cannot be blank");
            return;
        }

        if(PlayerAccountTable.emailExists(email)) {
            handler.status(400).result("An account with this email already exists.");
            return;
        }

        /* Create the account */
        // PlayerAccountTable.createAccount(username, email, password); //TODO Send back the created account's details or a success message after implementing the method in PlayerAccountTable
        // handler.status(200).result("Account created successfully.");
    }
}
