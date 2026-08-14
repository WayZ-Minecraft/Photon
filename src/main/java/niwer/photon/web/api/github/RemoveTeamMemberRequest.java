package niwer.photon.web.api.github;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import niwer.lumen.Console;
import niwer.photon.Directories;
import niwer.photon.PhotonEngine;
import niwer.photon.util.PhotonLogTypes;
import niwer.photon.web.api.ApiRequest;

/**
 * This class is used to remove a member from an organization team on GitHub.
 * 
 * @author Niwer
 */
public class RemoveTeamMemberRequest extends ApiRequest {

    private final transient String githubUsername;
    private final transient String TEAM_SLUG = Directories.getConfig().github_customer_team;

    public RemoveTeamMemberRequest(String githubUsername) {
        this.githubUsername = githubUsername;
    }

    @Override
    public String url() {
        return String.format("https://api.github.com/orgs/%s/teams/%s/memberships/%s", Directories.getConfig().github_new_repo_owner, TEAM_SLUG, this.githubUsername);
    }

    @Override
    public void request() {
        try {
            final HttpClient CLIENT = HttpClient.newHttpClient();
            final HttpRequest REQUEST = this.prepareRequest().DELETE().build(); // Uses HTTP DELETE without a request body in order to remove the user from the team
            final HttpResponse<String> RESPONSE = CLIENT.send(REQUEST, HttpResponse.BodyHandlers.ofString());

            switch (RESPONSE.statusCode()) {
                case 204 -> Console.log(String.format("User '%s' successfully removed from team '%s'.", this.githubUsername, TEAM_SLUG)).type(PhotonLogTypes.WEB_SERVER).container(PhotonEngine.LOGGER).send();
                default  -> Console.log(String.format("Error (%d) : %s", RESPONSE.statusCode(), RESPONSE.body())).type(PhotonLogTypes.WEB_SERVER).container(PhotonEngine.LOGGER).send();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}