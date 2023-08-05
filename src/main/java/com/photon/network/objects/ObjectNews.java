package com.photon.network.objects;

import java.sql.Date;



public class ObjectNews
{
	public int id;
	public String title;
	public String content;
	public Date date;
	public String imageUrl;
	
	/**
	 * Make a news object
	 * @param title the title of the news
	 * @param content the content of the news
	 * @param date the date when the news is create
	 * @param image the image of the news
	 */
	public ObjectNews(String title, String content, Date date, String imageUrl){
		this.title = title;
		this.content = content;
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
	public ObjectNews(int id, String title, String content, Date date, String imageUrl){
		this(title, content, date, imageUrl);
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
	 * @return the content of the news
	 */
	public String getContent(){
		return this.content;
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
	

}
