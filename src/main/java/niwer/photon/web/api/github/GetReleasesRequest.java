package niwer.photon.web.api.github;

import java.lang.reflect.Type;
import java.net.http.HttpResponse;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.gson.reflect.TypeToken;

import niwer.lumen.Console;
import niwer.photon.PhotonEngine;
import niwer.photon.objects.ObjectGithubRelease;
import niwer.photon.objects.ObjectProduct;
import niwer.photon.util.GsonUtils;
import niwer.photon.util.PhotonLogTypes;
import niwer.photon.web.HttpMethod;

/**
 * Get the releases of a GitHub repository.
 * TODO : In the future, this should be replaced by modrinth for all projects that are hosted there.
 * 
 * @author Niwer
 */
public class GetReleasesRequest extends GithubApiRequest<List<ObjectGithubRelease>> {

    private final String owner;
    private final String repo;
    private final Set<String> excludedTags;

    public GetReleasesRequest(ObjectProduct product) {
        this(product.repoOwner(), product.repoName(), product.excludedReleaseTags());
    }

    public GetReleasesRequest(String owner, String repo, Set<String> excludedTags) {
        this.owner = owner;
        this.repo = repo;
        this.excludedTags = excludedTags == null ? Collections.emptySet() : excludedTags;
    }

    @Override
    public String url() { return "https://api.github.com/repos/" + owner + "/" + repo + "/releases?per_page=100"; }

    @Override
    public HttpMethod method() { return HttpMethod.GET; }

    @Override
    public List<ObjectGithubRelease> request() {
        try {
            final HttpResponse<String> RESPONSE = this.sendHttpRequest(HttpResponse.BodyHandlers.ofString());
            if (RESPONSE.statusCode() != 200) {
                Console.log("No releases found for %s/%s with status code %d", owner, repo, RESPONSE.statusCode()).type(PhotonLogTypes.WEB_SERVER).container(PhotonEngine.LOGGER).send();
                return Collections.emptyList();
            }

            final Type LIST_TYPE = new TypeToken<List<ObjectGithubRelease>>(){}.getType();
            final List<ObjectGithubRelease> RELEASES = GsonUtils.GSON.fromJson(RESPONSE.body(), LIST_TYPE);
            
            return RELEASES.stream()
                    .filter(release -> !release.draft) // Exclude draft releases
                    .filter(release -> !excludedTags.contains(release.tagName)) // Filter out unwanted versions
                    .filter(release -> release.assets != null && release.assets.stream().anyMatch(a -> a.name.endsWith(".jar"))) // Ensure the release has at least one .jar asset
                    .collect(Collectors.toList());
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }
}