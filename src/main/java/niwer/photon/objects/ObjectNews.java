package niwer.photon.objects;

import java.util.Date;

import niwer.photon.util.TranslationManager.Language;

import net.dv8tion.jda.api.EmbedBuilder;
import niwer.queryon.SQLSerializable;
import niwer.queryon.tables.api.IColumnField;

public class ObjectNews extends SQLSerializable<ObjectNews>
{
	@IColumnField(name = "id", primaryKey = true, autoIncrement = true)
	private int id;

	@IColumnField(name = "title", notNull = true, charLimit = 255)
	private String title;

	@IColumnField(name = "contentEn")
	private String contentEn;

	@IColumnField(name = "contentFr")
	private String contentFr;

	@IColumnField(name = "date", notNull = true)
	private Date date = new Date();

	@IColumnField(name = "image")
	private String imageUrl;
	
	public ObjectNews() {}

	/**
	 * Make a news object
	 * @param title the title of the news
	 * @param content the content of the news
	 * @param date the date when the news is create
	 * @param image the image of the news
	 */
	public ObjectNews(String title, String contentEn, String contentFr, Date date, String imageUrl){
		this.title = title;
		this.contentEn = contentEn;
		this.contentFr = contentFr;
		this.date = date;
		this.imageUrl = imageUrl;
	}

	/**
	 * Make a news object
	 * @param id the id of the news (in the database it's the primary key and auto increment, please don't give pif id)
	 * @param title the title of the news
	 * @param content the content of the news
	 * @param date the date when the news is create
	 * @param image the image of the news
	 */
	public ObjectNews(int id, String title, String contentEn, String contentFr, Date date, String imageUrl){
		this(title, contentEn, contentFr, date, imageUrl);
		this.id = id;
	}

	public int id() { return this.id; }

	public String title() { return this.title; }

	public String contentForLang(Language language) {
		return switch (language) {
			case ENGLISH -> this.contentEn;
			case FRENCH -> this.contentFr;
			default -> throw new IllegalStateException("Unexpected value: " + language);
		};
	}

	public Date date() { return this.date; }

	public String imageURL() { return this.imageUrl; }

	/**
	 * Create an embed with the news
	 * 
	 * @return the embed of the news
	 */
	public EmbedBuilder discordEmbed() {
		EmbedBuilder embed = new EmbedBuilder();
		embed.setTitle(this.title);
		embed.setDescription(String.format("""
			🇬🇧%s
			\n\n
			🇫🇷%s
		""", this.contentEn, this.contentFr));
		// embed.setDescription("🇬🇧"+this.contentEn+"\n\n🇫🇷"+this.contentFr);
		embed.setImage(this.imageUrl);
		embed.setTimestamp(this.date.toInstant());
		return embed;
	}
}