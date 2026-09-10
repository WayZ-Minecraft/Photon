package niwer.photon.objects;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import com.google.gson.annotations.SerializedName;

public class ObjectProduct implements IPayloadProvider {

    @SerializedName("id") private String id;
    @SerializedName("name") private String name;
    
    @SerializedName("is_license_required") private boolean is_license_required = true;
    @SerializedName("default_license_duration_days") private Long default_license_duration_days;

    @SerializedName("stripe_price_ids") private Set<String> stripe_price_ids;
    @SerializedName("is_subscription") private boolean is_subscription;

    @SerializedName("repo_owner") private String repoOwner;
    @SerializedName("repo_name") private String repoName;
    @SerializedName("excluded_release_tags") private Set<String> excluded_release_tags; // Excluded release tags (e.g., ["v0.1.0-alpha", "v1.0.4-broken"])

    public ObjectProduct() {}

    public ObjectProduct(String id, String name, Long defaultDurationDays) {
        this.id = id;
        this.name = name;
        this.default_license_duration_days = defaultDurationDays;
    }

    public ObjectProduct(String id, String name, Long defaultDurationDays, Set<String> stripePriceId) {
        this(id, name, defaultDurationDays);
        this.stripe_price_ids = stripePriceId;
    }

    public String id() { return this.id; }

    public String name() { return this.name; }

    public boolean isLicenseRequired() { return this.is_license_required; }

    public Long defaultLicenseDurationDays() { return this.default_license_duration_days; }

    public Set<String> stripePriceIds() { return this.stripe_price_ids; }

    public boolean isSubscription() { return this.is_subscription; }

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
    public Map<String, Object> payload() {
        final Map<String, Object> PAYLOAD = new LinkedHashMap<>();
        PAYLOAD.put("id", this.id);
        PAYLOAD.put("name", this.name);
        PAYLOAD.put("defaultLicenseDurationDays", this.default_license_duration_days);
        return PAYLOAD;
    }

    @Override
    public String toString() {
        return String.format("ObjectProduct{id='%s', name='%s', is_license_required=%s, default_license_duration_days=%s, stripe_price_ids=%s, repo_owner='%s', repo_name='%s', excluded_release_tags=%s}",
            id, name, is_license_required, default_license_duration_days, stripe_price_ids, repoOwner, repoName, excluded_release_tags);
    }
}
