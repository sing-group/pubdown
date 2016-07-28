package es.uvigo.ei.sing.pubdown.paperdown.downloader;

public interface Searcher {

	void downloadPapers(boolean isCompletePaper, boolean convertPDFtoTXT, boolean keepPDF, boolean directoryType,
			int downloadFrom, int downloadTo);

	int getResultSize();

	void addDownloadListener(DownloadListener downloadListener);

	void removeDownloadListener(DownloadListener downloadListener);

	void removeAllDownloadListeners();

	boolean containsDownloadListener(DownloadListener downloadListener);

}
