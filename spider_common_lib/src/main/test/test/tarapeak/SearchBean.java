package test.tarapeak;

public class SearchBean {
	private String id;
	private String siteID;
	private String query;
	private String date;
	private int date_range;
	private String currency;

	public String getSiteID() {
		return siteID;
	}
	public void setSiteID(String siteID) {
		this.siteID = siteID;
	}
	public String getQuery() {
		return query;
	}
	public void setQuery(String query) {
		this.query = query;
	}
	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}
	public int getDate_range() {
		return date_range;
	}
	public void setDate_range(int date_range) {
		this.date_range = date_range;
	}
	public String getCurrency() {
		return currency;
	}
	public void setCurrency(String currency) {
		this.currency = currency;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	
}
