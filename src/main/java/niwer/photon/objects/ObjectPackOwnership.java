package niwer.photon.objects;

import java.util.Date;

import niwer.queryon.SQLSerializable;
import niwer.queryon.tables.api.IColumnField;

public class ObjectPackOwnership extends SQLSerializable<ObjectPackOwnership> {

	@IColumnField(name = "user_email")
	private String userEmail;

	@IColumnField(name = "account_uuid")
	private String accountUuid;

	@IColumnField(name = "pack_id")
	private String packId;

	@IColumnField(name = "purchased_at")
	private Date purchasedAt;

	@IColumnField(name = "first_download_at")
	private Date firstDownloadAt;

	@IColumnField(name = "is_active")
	private Boolean isActive = Boolean.TRUE;

	@IColumnField(name = "claimed_successfully")
	private Boolean claimedSuccessfully = Boolean.FALSE;

	@IColumnField(name = "updated_at")
	private Date updatedAt;

	public ObjectPackOwnership() {}

	public ObjectPackOwnership(String userEmail, String accountUuid, String packId, Date purchasedAt, Date firstDownloadAt, Boolean isActive, Boolean claimedSuccessfully, Date updatedAt) {
		this.userEmail = userEmail;
		this.accountUuid = accountUuid;
		this.packId = packId;
		this.purchasedAt = purchasedAt;
		this.firstDownloadAt = firstDownloadAt;
		this.isActive = isActive;
		this.claimedSuccessfully = claimedSuccessfully;
		this.updatedAt = updatedAt;
	}

	public String userEmail() { return userEmail; }

    public String getUserEmail() { return userEmail; }

	public String accountUuid() { return accountUuid; }

    public String getAccountUuid() { return accountUuid; }

	public String packId() { return packId; }

    public String getPackId() { return packId; }

	public Date purchasedAt() { return purchasedAt; }

    public Date getPurchasedAt() { return purchasedAt; }

	public Date firstDownloadAt() { return firstDownloadAt; }

    public Date getFirstDownloadAt() { return firstDownloadAt; }

	public Boolean isActive() { return isActive; }

    public Boolean getIsActive() { return isActive; }

	public Boolean claimedSuccessfully() { return claimedSuccessfully; }

    public Boolean getClaimedSuccessfully() { return claimedSuccessfully; }

	public Date updatedAt() { return updatedAt; }

    public Date getUpdatedAt() { return updatedAt; }
}