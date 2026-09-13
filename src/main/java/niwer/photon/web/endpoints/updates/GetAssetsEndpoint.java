package niwer.photon.web.endpoints.updates;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.google.gson.annotations.SerializedName;

import io.javalin.http.Context;
import niwer.photon.Directories;
import niwer.photon.objects.ObjectGithubRelease;
import niwer.photon.objects.ObjectGithubTag;
import niwer.photon.objects.ObjectProduct;
import niwer.photon.web.HttpMethod;
import niwer.photon.web.api.github.GetReleasesRequest;
import niwer.photon.web.api.github.GetTagRequest;
import niwer.photon.web.api.github.GetTagShaRequest;
import niwer.photon.web.endpoints.IEndpoint;

/**
 * Serves mod update files from GitHub releases.
 * TODO: Replace with Modrinth for hosted projects.
 * 
 * @author Niwer
 */
public class GetAssetsEndpoint implements IEndpoint {

    private static final ExecutorService EXECUTOR = Executors.newVirtualThreadPerTaskExecutor();

    @Override public String path() { return "/download/list"; }
    
    @Override public HttpMethod method() { return HttpMethod.GET; }

    @Override
    public void handle(Context ctx) {
        IEndpoint.setupRateLimit(ctx, 10, TimeUnit.MINUTES);

        /* Filter products having a repo */
        final List<ObjectProduct> PRODUCTS = Directories.getConfig().getProducts().stream().parallel().filter(ObjectProduct::hasRepo).toList();

        /* Futures */
        final List<CompletableFuture<Map.Entry<String, ReleaseResult>>> FUTURES = PRODUCTS.stream().map(this::fetchProductAssetsAsync).toList();

        /* Aggregate the results */
        final CompletableFuture<Map<String, ReleaseResult>> AGGREGATED_RESULT = CompletableFuture
            .allOf(FUTURES.toArray(CompletableFuture[]::new))
            .thenApply(ignored -> FUTURES.stream()
                .map((CompletableFuture<Map.Entry<String, ReleaseResult>> future) -> future.join())
                .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    Map.Entry::getValue,
                    (existing, replacement) -> existing
                ))
            );

        /* Send the aggregated result */
        ctx.future(() -> AGGREGATED_RESULT
            .thenAccept(ctx::json)
            .exceptionally(e -> {
                e.printStackTrace();
                ctx.status(500).json(Map.of("error", e.getMessage() != null ? e.getMessage() : "Unknown error"));
                return null;
            })
        );
    }

    private CompletableFuture<Map.Entry<String, ReleaseResult>> fetchProductAssetsAsync(final ObjectProduct product) {
        return CompletableFuture.supplyAsync(() -> {
            /* Fetch releases for the product */
            final List<ObjectGithubRelease> RELEASES = new GetReleasesRequest(product).request();

            /* Asynchronously resolve tag commit metadata for each release concurrently */
            final List<CompletableFuture<ObjectGithubTag>> TAG_FUTURES = RELEASES.stream()
                .map(release -> fetchTagAsync(product, release))
                .toList();

            final List<ObjectGithubTag> TAGS = TAG_FUTURES.stream()
                .map((CompletableFuture<ObjectGithubTag> future) -> future.join())
                .filter(Objects::nonNull)
                .toList();

            final ReleaseResult RESULT = new ReleaseResult(RELEASES, TAGS);
            return Map.entry(product.repoKey(), RESULT);
        }, EXECUTOR);
    }

    private CompletableFuture<ObjectGithubTag> fetchTagAsync(final ObjectProduct product, final ObjectGithubRelease release) {
        return CompletableFuture.supplyAsync(() -> {
            final String SHA = new GetTagShaRequest(product, release.tagName).request();
            if (SHA == null) return null;

            final ObjectGithubTag TAG = new GetTagRequest(product, SHA).request();
            return TAG;
        }, EXECUTOR);
    }

    public record ReleaseResult(@SerializedName("releases") List<ObjectGithubRelease> releases, @SerializedName("tags") List<ObjectGithubTag> tags) {
        public ReleaseResult {
            releases = releases != null ? releases : List.of();
            tags = tags != null ? tags : List.of();
        }
    }
}