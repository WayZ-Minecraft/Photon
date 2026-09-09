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
    }
}
