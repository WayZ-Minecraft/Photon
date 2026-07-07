package niwer.photon.objects;

import java.util.Date;

import niwer.queryon.SQLSerializable;
import niwer.queryon.tables.api.IColumnField;

public class ObjectPackProduct extends SQLSerializable<ObjectPackProduct> {

	@IColumnField(name = "id", primaryKey = true, notNull = true)
	private String id;

	@IColumnField(name = "name", notNull = true)
	private String name;

	@IColumnField(name = "description")
	private String description;

	@IColumnField(name = "category")
	private String category;

	@IColumnField(name = "stripe_price_id", notNull = true)
	private String stripePriceId;

	@IColumnField(name = "file_path")
	private String filePath;

	@IColumnField(name = "version_number")
	private String versionNumber;

	@IColumnField(name = "status")
	private String status;

	@IColumnField(name = "created_at")
	private Date createdAt;

	@IColumnField(name = "updated_at")
	private Date updatedAt;

	public ObjectPackProduct() {}

	public ObjectPackProduct(String id, String name, String description, String category, String stripePriceId, String filePath, String versionNumber, String status, Date createdAt, Date updatedAt) {
		this.id = id;
		this.name = name;
		this.description = description;
		this.category = category;
		this.stripePriceId = stripePriceId;
		this.filePath = filePath;
		this.versionNumber = versionNumber;
		this.status = status;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}

	public String id() { return id; }

    public String getId() { return id; }

	public String name() { return name; }

    public String getName() { return name; }

	public String description() { return description; }

    public String getDescription() { return description; }

	public String category() { return category; }

    public String getCategory() { return category; }

	public String stripePriceId() { return stripePriceId; }

    public String getStripePriceId() { return stripePriceId; }

	public String filePath() { return filePath; }

    public String getFilePath() { return filePath; }

	public String versionNumber() { return versionNumber; }

    public String getVersionNumber() { return versionNumber; }

	public String status() { return status; }

    public String getStatus() { return status; }

	public Date createdAt() { return createdAt; }

    public Date getCreatedAt() { return createdAt; }

	public Date updatedAt() { return updatedAt; }

    public Date getUpdatedAt() { return updatedAt; }
}