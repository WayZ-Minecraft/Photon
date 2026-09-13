package niwer.photon.objects;

import com.google.gson.annotations.SerializedName;

public class ObjectGithubTagSha {

    @SerializedName("object") private ShaContainer object;

    public ShaContainer object() {
        return object;
    }

    public static class ShaContainer {
        @SerializedName("sha") private String sha;

        public String sha() {
            return sha;
        }
    }
}
