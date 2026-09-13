package niwer.photon.web.api.github;

import java.net.http.HttpResponse;

import niwer.lumen.Console;
import niwer.photon.PhotonEngine;
import niwer.photon.objects.ObjectGithubTag;
import niwer.photon.objects.ObjectProduct;
import niwer.photon.util.GsonUtils;
import niwer.photon.util.PhotonLogTypes;
import niwer.photon.web.HttpMethod;

/**
 * Get the tags of a GitHub repository.
 * TODO : In the future, this should be replaced by modrinth for all projects that are hosted there.
 * 
 * @author Niwer
 */
public class GetTagRequest extends GithubApiRequest<ObjectGithubTag> {

    private final String owner;
    private final String repo;
    private final String sha;

    public GetTagRequest(ObjectProduct product, String sha) {
        this(product.repoOwner(), product.repoName(), sha);
    }

    public GetTagRequest(String owner, String repo, String sha) {
        this.owner = owner;
        this.repo = repo;
        this.sha = sha;
    }

    @Override
    public String url() { return "https://api.github.com/repos/" + owner + "/" + repo + "/git/tags/" + sha; }

    @Override
    public HttpMethod method() { return HttpMethod.GET; }

    @Override
    public ObjectGithubTag request() {
        try {
            final HttpResponse<String> RESPONSE = this.sendHttpRequest(HttpResponse.BodyHandlers.ofString());
            if (RESPONSE.statusCode() != 200) {
                Console.log("No tags found in %s/%s for %s with status code %d", owner, repo, sha, RESPONSE.statusCode()).type(PhotonLogTypes.WEB_SERVER).container(PhotonEngine.LOGGER).send();
                return null;
            }

            return GsonUtils.GSON.fromJson(RESPONSE.body(), ObjectGithubTag.class);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}