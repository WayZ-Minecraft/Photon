package niwer.photon.objects;

import java.util.List;

import com.google.gson.annotations.SerializedName;

public class ObjectGithubRelease {

    @SerializedName("id") public long id; 
    @SerializedName("tag_name") public String tagName;
    @SerializedName("name") public String name;
    @SerializedName("body") public String body;
    @SerializedName("draft") public boolean draft;
    @SerializedName("prerelease") public boolean prerelease;
    @SerializedName("published_at") public String publishedAt;
    @SerializedName("assets") public List<GithubAsset> assets;

    public boolean containsExecutable() {
        if (assets == null) return false;
        return assets.stream().anyMatch(asset -> asset.isExecutable());
    }

    public void keepOnlyExecutableAssets() {
        if (assets == null) return;
        assets.removeIf(asset -> !asset.isExecutable());
    }

    @Override
    public String toString() {
        return String.format("ObjectGithubRelease{id=%d, tagName='%s', name='%s', body='%s', draft=%b, prerelease=%b, publishedAt='%s'}", id, tagName, name, body, draft, prerelease, publishedAt);
    }

    public class GithubAsset {
        @SerializedName("id") public long id;
        @SerializedName("name") public String name;
        @SerializedName("size") public long size;
        @SerializedName("content_type") public String contentType;
        @SerializedName("download_count") public int downloadCount;

        public boolean isExecutable() {
            return name.endsWith(".jar")

                /* Microsoft */
                || name.endsWith(".exe")
                || name.endsWith(".msi")
                || name.endsWith(".bat")

                /* Apple */
                || name.endsWith(".dmg")

                /* Linux */
                || name.endsWith(".sh")
                || name.endsWith(".AppImage")
                || name.endsWith(".deb")
                || name.endsWith(".rpm")
                || name.endsWith(".apk")
            ;
        }
    }
}
