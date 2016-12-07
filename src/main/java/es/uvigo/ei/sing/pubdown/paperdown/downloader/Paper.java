package es.uvigo.ei.sing.pubdown.paperdown.downloader;

public class Paper {
	private String title;
	private String completeTitle;
	private boolean full;
	private boolean abst;

	public Paper(final String title, final String completeTitle) {
		this.title = title;
		this.completeTitle = completeTitle;
		this.full = false;
		this.abst = false;
	}

	public Paper(final String title, final String completeTitle, final boolean full, final boolean abst) {
		this.title = title;
		this.completeTitle = completeTitle;
		this.full = full;
		this.abst = abst;
	}

	public String getTitle() {
		return title;
	}

	public String getCompleteTitle() {
		return completeTitle;
	}

	public boolean isFull() {
		return full;
	}

	public void setFull(boolean full) {
		this.full = full;
	}

	public boolean isAbst() {
		return abst;
	}

	public void setAbst(boolean abst) {
		this.abst = abst;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((title == null) ? 0 : title.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Paper other = (Paper) obj;
		if (title == null) {
			if (other.title != null)
				return false;
		} else if (!title.equals(other.title))
			return false;
		return true;
	}

}
