package es.uvigo.ei.sing.pubdown.paperdown.downloader.pubmed;

import java.util.LinkedList;
import java.util.List;

public class PubMed {
	private String id = "";
	private String paperUrl = "";
	private String doi = "";
	private String paperTitle = "";
	private String completePaperTitle = "";
	private String date = "";
	private List<String> authorList;
	private boolean isCompletePaper;

	public PubMed() {
		authorList = new LinkedList<>();
	}

	public PubMed(String id, String paperUrl, String doi, String paperTitle, String completePaperTitle, String date,
			List<String> authorList, boolean isCompletePaper) {
		this.id = id;
		this.paperUrl = paperUrl;
		this.doi = doi;
		this.paperTitle = paperTitle;
		this.completePaperTitle = completePaperTitle;
		this.date = date;
		this.authorList = authorList;
		this.isCompletePaper = isCompletePaper;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getPaperUrl() {
		return paperUrl;
	}

	public void setPaperUrl(String paperUrl) {
		this.paperUrl = paperUrl;
	}

	public String getDoi() {
		return doi;
	}

	public void setDoi(String doi) {
		this.doi = doi;
	}

	public String getPaperTitle() {
		return paperTitle;
	}

	public void setPaperTitle(String paperTitle) {
		this.paperTitle = paperTitle;
	}

	public String getCompletePaperTitle() {
		return completePaperTitle;
	}

	public void setCompletePaperTitle(String completePaperTitle) {
		this.completePaperTitle = completePaperTitle;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public List<String> getAuthorList() {
		return authorList;
	}

	public void setAuthorList(List<String> authorList) {
		this.authorList = authorList;
	}

	public boolean isCompletePaper() {
		return isCompletePaper;
	}

	public void setCompletePaper(boolean isComplete) {
		this.isCompletePaper = isComplete;
	}
}
