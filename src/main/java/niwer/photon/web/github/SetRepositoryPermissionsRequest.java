package niwer.photon.web.github;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import com.google.gson.annotations.SerializedName;

import niwer.lumen.Console;
import niwer.photon.Directories;
import niwer.photon.PhotonEngine;
import niwer.photon.util.PhotonLogTypes;

/**
 * This class is used to set the permissions of a user for a specific repository on GitHub.
 * 
 * @author Niwer
 */
public class SetRepositoryPermissionsRequest extends ApiRequest {

    @SerializedName("permission")
    private final String permission; // "admin", "maintain", "write", "triage", or "read"

    private final transient String repositoryName;
    private final transient String githubUsername;

    public SetRepositoryPermissionsRequest(String githubUsername) { this(githubUsername, "admin"); }

    public SetRepositoryPermissionsRequest(String githubUsername, String permission) {
        this.githubUsername = githubUsername;
        this.repositoryName = Directories.getConfig().github_template_repo + "-" + this.sanitizeAndLower(githubUsername);
        this.permission = permission != null ? permission : "admin";
    }

    @Override
    public String url() {
        return String.format("https://api.github.com/repos/%s/%s/collaborators/%s", Directories.getConfig().github_new_repo_owner, this.repositoryName, this.githubUsername);
    }

    @Override
    public void request() {
        try {
            final HttpClient CLIENT = HttpClient.newHttpClient();
            final HttpRequest REQUEST = this.prepareRequest().PUT(this.prepareBody()).build();
            final HttpResponse<String> RESPONSE = CLIENT.send(REQUEST, HttpResponse.BodyHandlers.ofString());

            switch(RESPONSE.statusCode()) {
                case 201 -> Console.log("Collaborator invitation sent successfully for " + this.githubUsername).type(PhotonLogTypes.WEB_SERVER).container(PhotonEngine.LOGGER).send();
                case 204 -> Console.log("Permission updated successfully for " + this.githubUsername).type(PhotonLogTypes.WEB_SERVER).container(PhotonEngine.LOGGER).send();
                default -> Console.log("Error (" + RESPONSE.statusCode() + ") : " + RESPONSE.body()).type(PhotonLogTypes.WEB_SERVER).container(PhotonEngine.LOGGER).send();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}