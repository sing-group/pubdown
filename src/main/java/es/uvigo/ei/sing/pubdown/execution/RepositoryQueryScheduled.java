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

				scopusDownloadTo = scopusResult;

				if (scopusResult > 6000) {
					scopusDownloadTo = 6000;
				}

				repositoryQuery.setScopusDownloadTo(scopusDownloadTo);
			}
		}

		if (repositoryQuery.isPubmed()) {
			pubmedDownloader = new PubMedDownloader(query, directoryPath);
			pubmedResult = pubmedDownloader.getResultSize();
			if (pubmedResult != 0) {

				pubmedDownloadTo = pubmedResult;

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

		final int repositoryDownloadLimit = repositoryQuery.getRepository().getDownloadLimit();

		if (repository.isFulltextPaper()) {
			final boolean directoryType = repositoryQuery.isGroupBy();

			if (!repositoryQuery.isDownloadFirst()) {
				downloadFromScopusFullText(startsDownloadFrom, scopusDownloader, scopusReady, repository,
						repositoryDownloadLimit, directoryType);

				downloadFromPubMedFullText(startsDownloadFrom, pubmedDownloader, pubmedReady, repository,
						repositoryDownloadLimit, directoryType);
			} else {
				downloadFromPubMedFullText(startsDownloadFrom, pubmedDownloader, pubmedReady, repository,
						repositoryDownloadLimit, directoryType);

				downloadFromScopusFullText(startsDownloadFrom, scopusDownloader, scopusReady, repository,
						repositoryDownloadLimit, directoryType);
			}
		}
		if (repository.isAbstractPaper()) {
			final boolean directoryType = repositoryQuery.isGroupBy();

			if (!repositoryQuery.isDownloadFirst()) {
				downloadFromScopusAbstract(startsDownloadFrom, scopusDownloader, scopusReady, repository,
						repositoryDownloadLimit, directoryType);
				downloadFromPubMedAbstract(startsDownloadFrom, pubmedDownloader, pubmedReady, repository,
						repositoryDownloadLimit, directoryType);
			} else {
				downloadFromPubMedAbstract(startsDownloadFrom, pubmedDownloader, pubmedReady, repository,
						repositoryDownloadLimit, directoryType);
				downloadFromScopusAbstract(startsDownloadFrom, scopusDownloader, scopusReady, repository,
						repositoryDownloadLimit, directoryType);
			}

		}
	}

	private void downloadFromPubMedAbstract(final int startsDownloadFrom, PubMedDownloader pubmedDownloader,
			boolean pubmedReady, final Repository repository, final int repositoryDownloadLimit,
			final boolean directoryType) {
		if (repositoryQuery.isPubmed() && pubmedReady) {
			pubmedDownloader.downloadPapers(false, repository.isPdfToText(), repository.isKeepPdf(), directoryType,
					repositoryDownloadLimit, startsDownloadFrom, repositoryQuery.getPubmedDownloadTo());
		}
	}

	private void downloadFromScopusAbstract(final int startsDownloadFrom, ScopusDownloader scopusDownloader,
			boolean scopusReady, final Repository repository, int repositoryDownloadLimit,
			final boolean directoryType) {
		if (repositoryQuery.isScopus() && scopusReady) {
			if (repositoryDownloadLimit > 6000) {
				repositoryDownloadLimit = 6000;
			}

			scopusDownloader.downloadPapers(false, repository.isPdfToText(), repository.isKeepPdf(), directoryType,
					repositoryDownloadLimit, startsDownloadFrom, repositoryQuery.getScopusDownloadTo());
		}
	}

	private void downloadFromScopusFullText(final int startsDownloadFrom, ScopusDownloader scopusDownloader,
			boolean scopusReady, final Repository repository, int repositoryDownloadLimit,
			final boolean directoryType) {
		if (repositoryQuery.isScopus() && scopusReady) {
			if (repositoryDownloadLimit > 6000) {
				repositoryDownloadLimit = 6000;
			}
			scopusDownloader.downloadPapers(true, repository.isPdfToText(), repository.isKeepPdf(), directoryType,
					repositoryDownloadLimit, startsDownloadFrom, repositoryQuery.getScopusDownloadTo());
		}
	}

	private void downloadFromPubMedFullText(final int startsDownloadFrom, PubMedDownloader pubmedDownloader,
			boolean pubmedReady, final Repository repository, final int repositoryDownloadLimit,
			final boolean directoryType) {
		if (repositoryQuery.isPubmed() && pubmedReady) {
			pubmedDownloader.downloadPapers(true, repository.isPdfToText(), repository.isKeepPdf(), directoryType,
					repositoryDownloadLimit, startsDownloadFrom, repositoryQuery.getPubmedDownloadTo());
		}
	}

	private boolean isValidApiKey(String scopusApiKey) {
		return scopusApiKey != null && !scopusApiKey.isEmpty();
	}
}
