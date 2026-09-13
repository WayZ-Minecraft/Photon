package niwer.photon.web.api.github;

import java.net.http.HttpResponse;

import niwer.lumen.Console;
import niwer.photon.PhotonEngine;
import niwer.photon.objects.ObjectGithubTagSha;
import niwer.photon.objects.ObjectProduct;
import niwer.photon.util.GsonUtils;
import niwer.photon.util.PhotonLogTypes;
import niwer.photon.web.HttpMethod;

/**
 * Get the sha of tags inside a GitHub repository.
 * TODO : In the future, this should be replaced by modrinth for all projects that are hosted there.
 * 
 * @author Niwer
 */
public class GetTagShaRequest extends GithubApiRequest<String> {

    private final String owner;
    private final String repo;
    private final String tagName;

    public GetTagShaRequest(ObjectProduct product, String tagName) {
        this(product.repoOwner(), product.repoName(), tagName);
    }

    public GetTagShaRequest(String owner, String repo, String tagName) {
        this.owner = owner;
        this.repo = repo;
        this.tagName = tagName;
    }

    @Override
    public String url() { return "https://api.github.com/repos/" + owner + "/" + repo + "/git/ref/tags/" + tagName; }

    @Override
    public HttpMethod method() { return HttpMethod.GET; }

    @Override
    public String request() {
        try {
            final HttpResponse<String> RESPONSE = this.sendHttpRequest(HttpResponse.BodyHandlers.ofString());
            if (RESPONSE.statusCode() != 200) {
                Console.log("No tags found in %s/%s for %s with status code %d", owner, repo, tagName, RESPONSE.statusCode()).type(PhotonLogTypes.WEB_SERVER).container(PhotonEngine.LOGGER).send();
                return null;
            }

            final ObjectGithubTagSha DATA = GsonUtils.GSON.fromJson(RESPONSE.body(), ObjectGithubTagSha.class);
            return DATA.object() != null ? DATA.object().sha() : null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}