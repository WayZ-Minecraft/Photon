package com.photon.network.objects;


import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import net.dv8tion.jda.api.EmbedBuilder;

public class ObjectNews
{
	private int id;
	private final String title;
	private final Map<String, String> content = new HashMap<String, String>();
	private final Date date;
	private final String imageUrl;
	
	/**
	 * Make a news object
	 * @param title the title of the news
	 * @param content the content of the news
	 * @param date the date when the news is create
	 * @param image the image of the news
	 */
	public ObjectNews(String title, String contentEn, String contentFr, Date date, String imageUrl){
		this.title = title;
		this.content.put("en", contentEn);
		this.content.put("fr", contentFr);
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
	/**
	 * @return the id of the news
	 */
	public int getId(){
		return this.id;
	}

	/**
	 * @return the title of the news
	 */
	public String getTitle(){
		return this.title;
	}

	/**
	 * Return a HashMap with the content of the news (key: language, value: content)
	 * @return the content of the news
	 */
	public Map<String, String> getContent(){
		return this.content;
	}

	public String getContent(String language){
		return this.content.get(language);
	}

	/**
	 * @return the date of the news
	 */
	public Date getDate(){
		return this.date;
	}

	/**
	 * @return the image of the news
	 */
	public String getImageUrl(){
		return this.imageUrl;
	}

	/**
	 * Create an embed with the news
	 * @return the embed of the news
	 */
	public EmbedBuilder getEmbed(){
		EmbedBuilder embed = new EmbedBuilder();
		embed.setTitle(this.title);
		embed.setDescription("🇬🇧"+this.content.get("en")+"\n\n🇫🇷"+this.content.get("fr"));
		embed.setImage(this.imageUrl);
		embed.setTimestamp(this.date.toInstant());
		return embed;
	}
}
