package niwer.photon.objects;

import java.util.Collections;
import java.util.Set;

import com.google.gson.annotations.SerializedName;

public class ObjectProduct {

    @SerializedName("id") private String id;
    @SerializedName("name") private String name;
    
    @SerializedName("is_license_required") private boolean is_license_required = true;
    @SerializedName("default_license_duration_days") private Long default_license_duration_days;

    @SerializedName("stripe_price_id") private String stripe_price_id;

    @SerializedName("repo_owner") private String repoOwner;
    @SerializedName("repo_name") private String repoName;
    @SerializedName("excluded_release_tags") private Set<String> excluded_release_tags; // Excluded release tags (e.g., ["v0.1.0-alpha", "v1.0.4-broken"])

    public ObjectProduct() {}

    public ObjectProduct(String id, String name, Long defaultDurationDays) {
        this.id = id;
        this.name = name;
        this.default_license_duration_days = defaultDurationDays;
    }

    public ObjectProduct(String id, String name, Long defaultDurationDays, String stripePriceId) {
        this(id, name, defaultDurationDays);
        this.stripe_price_id = stripePriceId;
    }

    public String id() { return this.id; }

    public String name() { return this.name; }

    public boolean isLicenseRequired() { return this.is_license_required; }

    public Long defaultLicenseDurationDays() { return this.default_license_duration_days; }

    public String stripePriceId() { return this.stripe_price_id; }

    public String repoOwner() { return this.repoOwner; }

    public String repoName() { return this.repoName; }
    
    public String repoKey() { return this.repoOwner + "/" + this.repoName; }

    public Set<String> excludedReleaseTags() {
        return this.excluded_release_tags == null ? Collections.emptySet() : this.excluded_release_tags;
    }

    /**
     * @return True if the config for this product has repo owner and has a repo name.
     */
    public boolean hasRepo() {
        return (this.repoOwner != null && !this.repoOwner.isEmpty())
            && (this.repoName != null && !this.repoName.isEmpty());
    }

    @Override
    public String toString() {
        return String.format("ObjectProduct{id='%s', name='%s', is_license_required=%s, default_license_duration_days=%s, stripe_price_id='%s', repo_owner='%s', repo_name='%s', excluded_release_tags=%s}",
            id, name, is_license_required, default_license_duration_days, stripe_price_id, repoOwner, repoName, excluded_release_tags);
    }
}
