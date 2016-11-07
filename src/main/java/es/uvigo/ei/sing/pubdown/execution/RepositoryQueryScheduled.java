package es.uvigo.ei.sing.pubdown.execution;

import es.uvigo.ei.sing.pubdown.paperdown.downloader.pubmed.PubMedDownloader;
import es.uvigo.ei.sing.pubdown.paperdown.downloader.scopus.ScopusDownloader;
import es.uvigo.ei.sing.pubdown.web.entities.Repository;
import es.uvigo.ei.sing.pubdown.web.entities.RepositoryQuery;

public class RepositoryQueryScheduled {
	private RepositoryQuery repositoryQuery;
	private String directoryPath;
	private boolean toCheck;

	public RepositoryQueryScheduled() {

	}

	public RepositoryQueryScheduled(RepositoryQuery repositoryQuery) {
		this.repositoryQuery = repositoryQuery;
		this.directoryPath = "";
		this.toCheck = false;
	}

	public RepositoryQueryScheduled(RepositoryQuery repositoryQuery, String directoryPath, boolean toCheck) {
		this.repositoryQuery = repositoryQuery;
		this.directoryPath = directoryPath;
		this.toCheck = toCheck;
	}

	public RepositoryQuery getRepositoryQuery() {
		return repositoryQuery;
	}

	public void setRepositoryQuery(RepositoryQuery repositoryQuery) {
		this.repositoryQuery = repositoryQuery;
	}

	public String getDirectoryPath() {
		return directoryPath;
	}

	public void setDirectoryPath(String directoryPath) {
		this.directoryPath = directoryPath;
	}

	public boolean isToCheck() {
		return toCheck;
	}

	public void setToCheck(boolean toCheck) {
		this.toCheck = toCheck;
	}

	public void getResultSize() {
		ScopusDownloader scopusDownloader = new ScopusDownloader();
		PubMedDownloader pubmedDownloader = new PubMedDownloader();

		int scopusResult = 0;
		int pubmedResult = 0;
		int scopusDownloadTo = 0;
		int pubmedDownloadTo = 0;

		final String query = repositoryQuery.getQuery().replace(" ", "+");

		final String scopusApiKey = repositoryQuery.getRepository().getUser().getApiKey();

		if (repositoryQuery.isScopus()) {
			scopusDownloader = new ScopusDownloader(query, scopusApiKey, directoryPath);

			scopusResult = scopusDownloader.getResultSize();
			if (scopusResult != 0) {

				// scopusDownloadTo = scopusResult;
				//
				// if (scopusResult > 6000) {
				// scopusDownloadTo = 6000;
				// }

				scopusDownloadTo = 3;

				repositoryQuery.setScopusDownloadTo(scopusDownloadTo);
			}
		}

		if (repositoryQuery.isPubmed()) {
			pubmedDownloader = new PubMedDownloader(query, directoryPath);
			pubmedResult = pubmedDownloader.getResultSize();
			if (pubmedResult != 0) {
				// pubmedDownloadTo = pubmedResult;

				pubmedDownloadTo = 3;

				repositoryQuery.setPubmedDownloadTo(pubmedDownloadTo);
			}
		}
	}

	public void getPapers() {
		final int startsDownloadFrom = 0;
		ScopusDownloader scopusDownloader = new ScopusDownloader();
		PubMedDownloader pubmedDownloader = new PubMedDownloader();
		boolean scopusReady = false;
		boolean pubmedReady = false;

		final String query = repositoryQuery.getQuery().replace(" ", "+");

		final String scopusApiKey = repositoryQuery.getRepository().getUser().getApiKey();

		final Repository repository = repositoryQuery.getRepository();

		if (isValidApiKey(scopusApiKey)) {
			if (repositoryQuery.isScopus() && (repositoryQuery.getScopusDownloadTo() != 0
					&& repositoryQuery.getScopusDownloadTo() != Integer.MAX_VALUE)) {
				scopusDownloader = new ScopusDownloader(query, scopusApiKey, directoryPath);
				scopusReady = true;
			}
		}

		if (repositoryQuery.isPubmed() && (repositoryQuery.getPubmedDownloadTo() != 0
				&& repositoryQuery.getPubmedDownloadTo() != Integer.MAX_VALUE)) {
			pubmedDownloader = new PubMedDownloader(query, directoryPath);
			pubmedReady = true;
		}

		if (repository.isFulltextPaper()) {
			final boolean directoryType = repositoryQuery.isGroupBy();
			if (repositoryQuery.isScopus() && scopusReady) {
				scopusDownloader.downloadPapers(true, repository.isPdfToText(), repository.isKeepPdf(), directoryType,
						repositoryQuery.getDownloadLimit(), startsDownloadFrom, repositoryQuery.getScopusDownloadTo());
			}

			if (repositoryQuery.isPubmed() && pubmedReady) {
				pubmedDownloader.downloadPapers(true, repository.isPdfToText(), repository.isKeepPdf(), directoryType,
						repositoryQuery.getDownloadLimit(), startsDownloadFrom, repositoryQuery.getPubmedDownloadTo());
			}
		}

		if (repository.isAbstractPaper()) {
			final boolean directoryType = repositoryQuery.isGroupBy();
			if (repositoryQuery.isScopus() && scopusReady) {
				scopusDownloader.downloadPapers(false, repository.isPdfToText(), repository.isKeepPdf(), directoryType,
						repositoryQuery.getDownloadLimit(), startsDownloadFrom, repositoryQuery.getScopusDownloadTo());
			}
			if (repositoryQuery.isPubmed() && pubmedReady) {
				pubmedDownloader.downloadPapers(false, repository.isPdfToText(), repository.isKeepPdf(), directoryType,
						repositoryQuery.getDownloadLimit(), startsDownloadFrom, repositoryQuery.getPubmedDownloadTo());
			}
		}
	}

	private boolean isValidApiKey(String scopusApiKey) {
		return scopusApiKey != null && !scopusApiKey.isEmpty();
	}
}
