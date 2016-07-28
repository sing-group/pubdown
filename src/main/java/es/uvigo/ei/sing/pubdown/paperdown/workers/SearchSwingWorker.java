package es.uvigo.ei.sing.pubdown.paperdown.workers;

import java.awt.Cursor;

import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JTextField;
import javax.swing.SwingWorker;

import es.uvigo.ei.sing.pubdown.paperdown.downloader.Searcher;


public class SearchSwingWorker extends SwingWorker<Void, Integer> {

	private Searcher downloader;
	private int resultSize;
	private JTextField resultTextField;
	private JTextField downloadFromTextField;
	private JTextField downloadToTextField;
	private JCheckBox limitCheckBox;
	private int defaultSearchResultLimit;
	private JFrame frame;

	public SearchSwingWorker() {

	}

	public SearchSwingWorker(JFrame frame, Searcher downloader, int resultSize, JTextField resultTextField,
			JTextField downloadFromTextField, JTextField downloadToTextField, JCheckBox limitCheckBox,
			int defaultSearchResultLimit) {
		super();
		this.frame = frame;
		this.frame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		this.downloader = downloader;
		this.resultSize = resultSize;
		this.resultTextField = resultTextField;
		this.downloadFromTextField = downloadFromTextField;
		this.downloadToTextField = downloadToTextField;
		this.limitCheckBox = limitCheckBox;
		this.defaultSearchResultLimit = defaultSearchResultLimit;
	}

	@Override
	protected Void doInBackground() throws Exception {
		// this.scopusResultSize = this.scopusDownloader.getResultSize();
		//
		// this.scopusResultsTextField.setText(String.valueOf(this.scopusResultSize));
		// if (this.scopusResultSize < DEFAULT_RESULT_LIMIT) {
		// this.scopusDownloadToTextField.setText(String.valueOf(this.scopusResultSize));
		// }
		//
		// this.scopusLimitCheckBox.setEnabled(true);
		// this.scopusDownloadFromTextField.setEnabled(true);
		// this.scopusDownloadToTextField.setEnabled(true);
		// this.scopusReady = true;

		this.resultSize = this.downloader.getResultSize();

		this.resultTextField.setText(String.valueOf(this.resultSize));
		if (this.resultSize < this.defaultSearchResultLimit) {
			this.downloadToTextField.setText(String.valueOf(this.resultSize));
		}
		this.limitCheckBox.setEnabled(true);
		this.downloadFromTextField.setEnabled(true);
		this.downloadToTextField.setEnabled(true);
		this.frame.setCursor(Cursor.getDefaultCursor());

		return null;
	}

}
