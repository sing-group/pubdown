package es.uvigo.ei.sing.pubdown.paperdown.workers;

import javax.swing.SwingWorker;

import es.uvigo.ei.sing.pubdown.paperdown.downloader.DownloadListener;
import es.uvigo.ei.sing.pubdown.paperdown.downloader.Searcher;

public class ProgressBarSwingWorker extends SwingWorker<Void, Integer> {

	private Searcher downloader;
	private boolean isCompletePaper;
	private boolean convertPDFtoTXT;
	private boolean keepPDF;
	private boolean directoryType;
	private int downloadFrom;
	private int downloadTo;

	public ProgressBarSwingWorker() {

	}

	public ProgressBarSwingWorker(final Searcher downloader, final boolean isCompletePaper,
			final boolean converPDFToTXT, final boolean keepPDF, final boolean directoryType, final int downloadFrom,
			final int downloadTo, final DownloadListener downloadListener) {
		super();
		this.downloader = downloader;
		this.isCompletePaper = isCompletePaper;
		this.convertPDFtoTXT = converPDFToTXT;
		this.keepPDF = keepPDF;
		this.directoryType = directoryType;
		this.downloadFrom = downloadFrom;
		this.downloadTo = downloadTo;

		if (!this.downloader.containsDownloadListener(downloadListener)) {
			this.downloader.addDownloadListener(downloadListener);
		}

	}

	@Override
	protected Void doInBackground() throws Exception {
		this.downloader.downloadPapers(this.isCompletePaper, this.convertPDFtoTXT, this.keepPDF, this.directoryType,
				this.downloadFrom, this.downloadTo);

		return null;
	}

}
