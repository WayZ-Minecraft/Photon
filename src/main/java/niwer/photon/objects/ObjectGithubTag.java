package niwer.photon.objects;

import com.google.gson.annotations.SerializedName;

public class ObjectGithubTag {
    @SerializedName("tag") public String tag;
    @SerializedName("sha") public String sha;
    @SerializedName("message") public String message;
}
