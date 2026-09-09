package niwer.photon.web.endpoints.updates;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import io.javalin.http.Context;
import niwer.photon.Directories;
import niwer.photon.objects.ObjectGithubRelease;
import niwer.photon.objects.ObjectProduct;
import niwer.photon.web.HttpMethod;
import niwer.photon.web.api.github.GetReleasesRequest;
import niwer.photon.web.endpoints.IEndpoint;

/**
 * Serves mod update files from GitHub releases.
 * TODO : In the future, this should be replaced by modrinth for all projects that are hosted there.
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

        /* Start a new task for each product */
        List<CompletableFuture<Map.Entry<String, List<ObjectGithubRelease>>>> futures = PRODUCTS.stream()
            .map(product -> CompletableFuture.supplyAsync(() -> {
                final List<ObjectGithubRelease> RELEASES = new GetReleasesRequest(product).request();
                return Map.entry(product.repoKey(), RELEASES);
            }, EXECUTOR))
            .toList();

        /* Wait for all tasks to finish */
        CompletableFuture<Map<String, List<ObjectGithubRelease>>> allFutures = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
            .thenApply(v -> futures.stream().map(CompletableFuture::join).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));

        /* Delegate the completion to Javalin (frees the server thread during processing) */
        ctx.future(() -> allFutures.thenAccept(result -> ctx.status(200).json(result)));
    }
}
