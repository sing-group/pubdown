package es.uvigo.ei.sing.pubdown.paperdown.gui;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import es.uvigo.ei.sing.pubdown.paperdown.downloader.DownloadListener;
import es.uvigo.ei.sing.pubdown.paperdown.downloader.pubmed.PubMedDownloader;
import es.uvigo.ei.sing.pubdown.paperdown.downloader.scopus.ScopusDownloader;
import es.uvigo.ei.sing.pubdown.paperdown.workers.ProgressBarSwingWorker;
import es.uvigo.ei.sing.pubdown.paperdown.workers.SearchSwingWorker;


public class PaperDownSwing extends JPanel {
	private static final long serialVersionUID = 1L;
	private static final int DEFAULT_RESULT_LIMIT = 1000;
	private static final int SCOPUS_LIMIT = 6000;

	private JLabel queryLabel;
	private JLabel scopusResultsLabel;
	private JLabel pubMedResultsLabel;
	private JLabel downloadTypeLabel;
	private JLabel selectFolderLabel;
	private JLabel scopusLimitLabel;
	private JLabel pubMedLimitLabel;
	private JLabel progressLabel;

	private JTextField queryTextField;
	private JTextField selectFolderTextField;
	private JTextField scopusResultsTextField;
	private JTextField pubMedResultsTextField;
	private JTextField scopusDownloadFromTextField;
	private JTextField pubMedDownloadFromTextField;
	private JTextField scopusDownloadToTextField;
	private JTextField pubMedDownloadToTextField;

	private JButton searchButton;
	private JButton selectFolderButton;
	private JButton downloadButton;

	private JCheckBox abstractCheckBox;
	private JCheckBox fullTextCheckBox;
	private JCheckBox scopusCheckBox;
	private JCheckBox pubMedCheckBox;
	private JCheckBox scopusLimitCheckBox;
	private JCheckBox pubMedLimitCheckBox;
	private JCheckBox convertPDFtoTXTCheckBox;
	private JCheckBox deletePDFCheckBox;

	private JRadioButton groupByPaperRadioButton;
	private JRadioButton groupByTypeRadioButton;
	private ButtonGroup buttonGroup;

	private JProgressBar progressBar;

	private String userApiKey = "";
	private String userDirectory = "";
	private int scopusResultSize = 0;
	private int pubMedResultSize = 0;
	private boolean isFolderSelected = false;
	private boolean scopusReady = false;
	private boolean pubMedReady = false;
	private ScopusDownloader scopusDownloader = new ScopusDownloader();
	private PubMedDownloader pubMedDownloader = new PubMedDownloader();

	public PaperDownSwing(JFrame frame) {
		super(new BorderLayout());
		this.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		createComponents();

		addComponents();

		this.queryTextField.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				if (PaperDownSwing.this.queryTextField.getText().isEmpty())
					PaperDownSwing.this.searchButton.setEnabled(false);
				else {
					PaperDownSwing.this.searchButton.setEnabled(true);
				}
			}
		});

		this.scopusDownloadFromTextField.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				if (Integer.parseInt(PaperDownSwing.this.scopusDownloadFromTextField.getText()) > Integer
						.parseInt(PaperDownSwing.this.scopusDownloadToTextField.getText())
						|| Integer.parseInt(PaperDownSwing.this.scopusDownloadFromTextField.getText()) > Integer
								.parseInt(PaperDownSwing.this.scopusDownloadToTextField.getText()) - 1) {
					PaperDownSwing.this.scopusDownloadFromTextField.setText(String.valueOf(0));
					SwingUtilities.invokeLater(() -> {
						final String message = "The \"from\" value cannot be equals/higher than the \"to\" value";
						final JFrame dialogFrame = new JFrame();
						dialogFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
						JOptionPane.showMessageDialog(dialogFrame, message);
					});
				}
			}
		});

		this.pubMedDownloadFromTextField.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				if (Integer.parseInt(PaperDownSwing.this.pubMedDownloadFromTextField.getText()) > Integer
						.parseInt(PaperDownSwing.this.pubMedDownloadToTextField.getText())
						|| Integer.parseInt(PaperDownSwing.this.pubMedDownloadFromTextField.getText()) > Integer
								.parseInt(PaperDownSwing.this.pubMedDownloadToTextField.getText()) - 1) {
					PaperDownSwing.this.pubMedDownloadFromTextField.setText(String.valueOf(0));
					SwingUtilities.invokeLater(() -> {
						final String message = "The \"from\" value cannot be equals/higher than the \"to\" value";
						final JFrame dialogFrame = new JFrame();
						dialogFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
						JOptionPane.showMessageDialog(dialogFrame, message);
					});
				}
			}
		});

		this.scopusDownloadToTextField.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				if (Integer.parseInt(PaperDownSwing.this.scopusDownloadToTextField
						.getText()) > PaperDownSwing.this.scopusResultSize) {

					System.out.println(Integer.parseInt(PaperDownSwing.this.scopusDownloadToTextField.getText()));

					System.out.println(PaperDownSwing.this.scopusResultSize);

					PaperDownSwing.this.scopusDownloadToTextField.setText(String.valueOf(scopusResultSize));
					SwingUtilities.invokeLater(() -> {
						final String message = "You can't download more than " + scopusResultSize + " articles";
						final JFrame dialogFrame = new JFrame();
						dialogFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
						JOptionPane.showMessageDialog(dialogFrame, message);
					});
				} else if (Integer.parseInt(PaperDownSwing.this.scopusDownloadToTextField.getText()) > SCOPUS_LIMIT) {
					PaperDownSwing.this.scopusDownloadToTextField.setText("6000");
					SwingUtilities.invokeLater(() -> {
						final String message = "You can't download more than 6000 Scopus articles";
						final JFrame dialogFrame = new JFrame();
						dialogFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
						JOptionPane.showMessageDialog(dialogFrame, message);
					});
				}
			}
		});

		this.pubMedDownloadToTextField.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				if (Integer.parseInt(PaperDownSwing.this.pubMedDownloadToTextField
						.getText()) > PaperDownSwing.this.pubMedResultSize) {
					PaperDownSwing.this.pubMedDownloadToTextField.setText(String.valueOf(pubMedResultSize));
					SwingUtilities.invokeLater(() -> {
						final String message = "You can't download more than " + pubMedResultSize + " articles";
						final JFrame dialogFrame = new JFrame();
						dialogFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
						JOptionPane.showMessageDialog(dialogFrame, message);
					});
				}
			}
		});

		this.scopusCheckBox.addItemListener(itemListener -> {
			SwingUtilities.invokeLater(() -> {
				if (itemListener.getStateChange() == ItemEvent.SELECTED) {
					final String message = "Insert your Scopus API Key";
					final JFrame dialogFrame = new JFrame();
					dialogFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
					final String apiKey = JOptionPane.showInputDialog(dialogFrame, message,
							"a1549163d9b16421237ec29c9bbbdf29");

					if (apiKey == null || apiKey.isEmpty()) {
						this.scopusCheckBox.setSelected(false);
					} else {
						PaperDownSwing.this.userApiKey = apiKey;
					}
				}
			});
		});

		this.scopusLimitCheckBox.addItemListener(itemListener -> {
			SwingUtilities.invokeLater(() -> {
				if (itemListener.getStateChange() == ItemEvent.SELECTED) {
					this.scopusDownloadFromTextField.setEnabled(true);
					this.scopusDownloadToTextField.setEnabled(true);
				} else {
					this.scopusDownloadFromTextField.setEnabled(false);
					this.scopusDownloadToTextField.setEnabled(false);
				}
			});
		});

		this.pubMedLimitCheckBox.addItemListener(itemListener -> {
			SwingUtilities.invokeLater(() -> {
				if (itemListener.getStateChange() == ItemEvent.SELECTED) {
					this.pubMedDownloadFromTextField.setEnabled(true);
					this.pubMedDownloadToTextField.setEnabled(true);
				} else {
					this.pubMedDownloadFromTextField.setEnabled(false);
					this.pubMedDownloadToTextField.setEnabled(false);
				}
			});
		});

		this.convertPDFtoTXTCheckBox.addItemListener(itemListener -> {
			SwingUtilities.invokeLater(() -> {
				if (itemListener.getStateChange() == ItemEvent.SELECTED) {
					this.deletePDFCheckBox.setEnabled(true);
				} else {
					this.deletePDFCheckBox.setEnabled(false);
				}
			});
		});

		this.searchButton.addActionListener(actionListener -> {
			if (this.scopusCheckBox.isSelected() || this.pubMedCheckBox.isSelected()) {
				this.scopusDownloadFromTextField.setText(String.valueOf(0));
				this.pubMedDownloadFromTextField.setText(String.valueOf(0));
				this.scopusDownloadToTextField.setText(String.valueOf(DEFAULT_RESULT_LIMIT));
				this.pubMedDownloadToTextField.setText(String.valueOf(DEFAULT_RESULT_LIMIT));
				this.scopusReady = false;
				this.pubMedReady = false;

				if (this.scopusCheckBox.isSelected()) {
					this.scopusDownloader = new ScopusDownloader(this.queryTextField.getText().replace(" ", "+"),
							this.userApiKey, this.userDirectory);
					this.scopusResultSize = this.scopusDownloader.getResultSize();

					// this.scopusResultsTextField.setText(String.valueOf(this.scopusResultSize));
					// if (this.scopusResultSize < DEFAULT_RESULT_LIMIT) {
					// this.scopusDownloadToTextField.setText(String.valueOf(this.scopusResultSize));
					// }
					// this.scopusLimitCheckBox.setEnabled(true);
					// this.scopusDownloadFromTextField.setEnabled(true);
					// this.scopusDownloadToTextField.setEnabled(true);

					this.scopusReady = true;

					new SearchSwingWorker(frame, this.scopusDownloader, this.scopusResultSize,
							this.scopusResultsTextField, this.scopusDownloadFromTextField,
							this.scopusDownloadToTextField, this.scopusLimitCheckBox, DEFAULT_RESULT_LIMIT).execute();
				}

				if (this.pubMedCheckBox.isSelected()) {
					this.pubMedDownloader = new PubMedDownloader(this.queryTextField.getText().replace(" ", "+"),
							this.userDirectory);
					this.pubMedResultSize = this.pubMedDownloader.getResultSize();

					// this.pubMedResultsTextField.setText(String.valueOf(this.pubMedResultSize));
					// if (this.pubMedResultSize < DEFAULT_RESULT_LIMIT) {
					// this.pubMedDownloadToTextField.setText(String.valueOf(this.pubMedResultSize));
					// }
					// this.pubMedLimitCheckBox.setEnabled(true);
					// this.pubMedDownloadFromTextField.setEnabled(true);
					// this.pubMedDownloadToTextField.setEnabled(true);

					this.pubMedReady = true;

					new SearchSwingWorker(frame, this.pubMedDownloader, this.pubMedResultSize,
							this.pubMedResultsTextField, this.pubMedDownloadFromTextField,
							this.pubMedDownloadToTextField, this.pubMedLimitCheckBox, DEFAULT_RESULT_LIMIT).execute();
				}
			}
		});

		this.selectFolderButton.addActionListener(actionListener -> {
			final JFileChooser chooser = new JFileChooser(System.getProperty("user.dir"));
			chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			if (chooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
				this.isFolderSelected = true;
				this.userDirectory = chooser.getSelectedFile().getAbsolutePath() + File.separator;
				this.selectFolderTextField.setText(this.userDirectory);
			}
		});

		this.downloadButton.addActionListener(actionListener -> {
			if ((this.fullTextCheckBox.isSelected() || this.abstractCheckBox.isSelected()) && isFolderSelected
					&& (this.scopusReady || this.pubMedReady)) {
				this.downloadButton.setEnabled(false);

				final boolean convertPDFtoTXT = this.convertPDFtoTXTCheckBox.isSelected();

				int scopusDownloadFrom = 0;
				int scopusDownloadTo = 0;
				int pubMedDownloadFrom = 0;
				int pubMedDownloadTo = 0;

				if (this.scopusCheckBox.isSelected()) {

					scopusDownloadFrom = this.scopusLimitCheckBox.isSelected()
							? Integer.parseInt(this.scopusDownloadFromTextField.getText()) : this.pubMedResultSize;

					scopusDownloadTo = this.scopusLimitCheckBox.isSelected()
							? Integer.parseInt(this.scopusDownloadToTextField.getText()) : this.scopusResultSize;
				}

				if (this.pubMedCheckBox.isSelected()) {

					pubMedDownloadFrom = this.pubMedLimitCheckBox.isSelected()
							? Integer.parseInt(this.pubMedDownloadFromTextField.getText()) : this.pubMedResultSize;

					pubMedDownloadTo = this.pubMedLimitCheckBox.isSelected()
							? Integer.parseInt(this.pubMedDownloadToTextField.getText()) : this.pubMedResultSize;
				}

				final boolean keepPDF = !(this.deletePDFCheckBox.isEnabled() && this.deletePDFCheckBox.isSelected());

				final boolean directoryType = this.groupByTypeRadioButton.isSelected();

				if (scopusDownloadTo > SCOPUS_LIMIT) {
					scopusDownloadTo = SCOPUS_LIMIT;
				}

				int numberOfPapers = (scopusDownloadTo - scopusDownloadFrom) + (pubMedDownloadTo - pubMedDownloadFrom);

				if (this.fullTextCheckBox.isSelected() && this.abstractCheckBox.isSelected()) {
					numberOfPapers = numberOfPapers * 2;
				}

				this.progressBar.setMaximum(numberOfPapers);
				this.progressBar.setValue(0);

				final DownloadListener downloadListener = downloadEvent -> {
					synchronized (PaperDownSwing.this.progressBar) {
						PaperDownSwing.this.progressBar.setValue(PaperDownSwing.this.progressBar.getValue() + 1);

						if (PaperDownSwing.this.progressBar.getMaximum() == PaperDownSwing.this.progressBar
								.getValue()) {
							PaperDownSwing.this.downloadButton.setEnabled(true);
							PaperDownSwing.this.scopusDownloader.removeAllDownloadListeners();
							PaperDownSwing.this.pubMedDownloader.removeAllDownloadListeners();
						}
					}
				};

				this.scopusDownloader.setDirectory(this.userDirectory);
				this.pubMedDownloader.setDirectory(this.userDirectory);

				if (this.fullTextCheckBox.isSelected()) {
					if (this.scopusReady && this.scopusCheckBox.isSelected()) {

						new ProgressBarSwingWorker(this.scopusDownloader, true, convertPDFtoTXT, keepPDF, directoryType,
								scopusDownloadFrom, scopusDownloadTo, downloadListener).execute();
					}

					if (this.pubMedReady && this.pubMedCheckBox.isSelected()) {

						new ProgressBarSwingWorker(this.pubMedDownloader, true, convertPDFtoTXT, keepPDF, directoryType,
								pubMedDownloadFrom, pubMedDownloadTo, downloadListener).execute();
					}
				}

				if (this.abstractCheckBox.isSelected()) {
					if (this.scopusReady && this.scopusCheckBox.isSelected()) {

						new ProgressBarSwingWorker(this.scopusDownloader, false, convertPDFtoTXT, keepPDF,
								directoryType, scopusDownloadFrom, scopusDownloadTo, downloadListener).execute();
					}

					if (this.pubMedReady && this.pubMedCheckBox.isSelected()) {

						new ProgressBarSwingWorker(this.pubMedDownloader, false, convertPDFtoTXT, keepPDF,
								directoryType, pubMedDownloadFrom, pubMedDownloadTo, downloadListener).execute();
					}
				}
			} else {
				String message = "Before download, you have to:\n";
				if (!this.scopusReady && !this.pubMedReady) {
					message = message + "Search in Scopus and/or PubMed\n";
				}
				if (!this.fullTextCheckBox.isSelected() && !this.abstractCheckBox.isSelected()) {
					message = message + "Select full text and/or abstract\n";
				}
				if (!isFolderSelected) {
					message = message + "An output folder";
				}
				final JFrame dialogFrame = new JFrame();
				dialogFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				JOptionPane.showMessageDialog(dialogFrame, message);
			}

		});
	}

	private void addComponents() {
		final JPanel panelNorth = new JPanel();
		final JPanel panelCenter = new JPanel();
		final GridBagLayout layoutNorth = new GridBagLayout();
		final GridBagLayout layoutCenter = new GridBagLayout();

		panelNorth.setLayout(layoutNorth);
		panelCenter.setLayout(layoutCenter);

		final GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 0d;
		gbc.weighty = 1d;
		gbc.gridx = 0;
		gbc.gridy = 0;
		panelNorth.add(this.queryLabel, gbc);

		gbc.gridx = 1;
		gbc.gridwidth = 3;
		gbc.weightx = 1d;
		gbc.anchor = GridBagConstraints.CENTER;
		panelNorth.add(this.queryTextField, gbc);

		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.gridwidth = 1;
		gbc.weightx = 0d;
		panelNorth.add(this.scopusCheckBox, gbc);

		gbc.gridx = 1;
		panelNorth.add(this.pubMedCheckBox, gbc);

		gbc.gridx = 3;
		gbc.fill = GridBagConstraints.NONE;
		gbc.anchor = GridBagConstraints.EAST;
		panelNorth.add(this.searchButton, gbc);
		this.searchButton.setEnabled(false);

		panelCenter.setBorder(BorderFactory.createTitledBorder("Query results"));

		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 0d;
		gbc.weighty = 1d;
		panelCenter.add(this.scopusResultsLabel, gbc);

		gbc.gridx = 1;
		gbc.weightx = 1d;
		this.scopusResultsTextField.setEditable(false);
		this.scopusResultsTextField.setHorizontalAlignment(SwingConstants.CENTER);
		panelCenter.add(this.scopusResultsTextField, gbc);

		gbc.weightx = 0d;
		gbc.weighty = 1d;
		gbc.gridx = 2;
		this.scopusLimitCheckBox.setEnabled(false);
		this.scopusLimitCheckBox.setSelected(true);
		panelCenter.add(this.scopusLimitCheckBox, gbc);

		gbc.gridx = 3;
		gbc.weightx = 1d;
		this.scopusDownloadFromTextField.setEnabled(false);
		this.scopusDownloadFromTextField.setHorizontalAlignment(SwingConstants.CENTER);
		panelCenter.add(this.scopusDownloadFromTextField, gbc);

		gbc.gridx = 4;
		gbc.weightx = 0d;
		gbc.insets = new Insets(0, 20, 0, 20);
		panelCenter.add(this.scopusLimitLabel, gbc);

		gbc.gridx = 5;
		gbc.weightx = 1d;
		gbc.insets = new Insets(0, 0, 0, 0);
		this.scopusDownloadToTextField.setEnabled(false);
		this.scopusDownloadToTextField.setHorizontalAlignment(SwingConstants.CENTER);
		panelCenter.add(this.scopusDownloadToTextField, gbc);

		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.weightx = 0d;
		gbc.weighty = 1d;
		panelCenter.add(pubMedResultsLabel, gbc);

		gbc.gridx = 1;
		gbc.weightx = 1d;
		this.pubMedResultsTextField.setEditable(false);
		this.pubMedResultsTextField.setHorizontalAlignment(SwingConstants.CENTER);
		panelCenter.add(this.pubMedResultsTextField, gbc);

		gbc.weightx = 0d;
		gbc.weighty = 1d;
		gbc.gridx = 2;
		this.pubMedLimitCheckBox.setEnabled(false);
		this.pubMedLimitCheckBox.setSelected(true);
		panelCenter.add(this.pubMedLimitCheckBox, gbc);

		gbc.gridx = 3;
		gbc.weightx = 1d;
		this.pubMedDownloadFromTextField.setEnabled(false);
		this.pubMedDownloadFromTextField.setHorizontalAlignment(SwingConstants.CENTER);
		panelCenter.add(this.pubMedDownloadFromTextField, gbc);

		gbc.gridx = 4;
		gbc.weightx = 0d;
		gbc.insets = new Insets(0, 20, 0, 20);
		panelCenter.add(this.pubMedLimitLabel, gbc);

		gbc.gridx = 5;
		gbc.weightx = 1d;
		gbc.insets = new Insets(0, 0, 0, 0);
		this.pubMedDownloadToTextField.setEnabled(false);
		this.pubMedDownloadToTextField.setHorizontalAlignment(SwingConstants.CENTER);
		panelCenter.add(this.pubMedDownloadToTextField, gbc);

		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.weightx = 1d;
		gbc.weighty = 1d;
		panelCenter.add(this.downloadTypeLabel, gbc);

		gbc.gridx = 1;
		panelCenter.add(this.abstractCheckBox, gbc);

		gbc.gridx = 2;
		panelCenter.add(this.fullTextCheckBox, gbc);

		gbc.gridx = 0;
		gbc.gridy = 3;
		gbc.weightx = 1d;
		gbc.weighty = 1d;
		gbc.gridwidth = 2;
		panelCenter.add(this.convertPDFtoTXTCheckBox, gbc);

		gbc.gridx = 2;
		gbc.weightx = 1d;
		gbc.weighty = 1d;
		gbc.gridwidth = 1;
		this.deletePDFCheckBox.setEnabled(false);
		panelCenter.add(this.deletePDFCheckBox, gbc);

		gbc.gridx = 1;
		gbc.gridy = 4;
		panelCenter.add(this.groupByTypeRadioButton, gbc);

		gbc.gridx = 2;
		this.groupByTypeRadioButton.setSelected(true);
		panelCenter.add(this.groupByPaperRadioButton, gbc);

		gbc.gridx = 0;
		gbc.gridy = 5;
		panelCenter.add(this.selectFolderLabel, gbc);

		gbc.gridx = 1;
		gbc.gridwidth = 3;
		this.selectFolderTextField.setEditable(false);
		panelCenter.add(this.selectFolderTextField, gbc);

		gbc.gridx = 4;
		gbc.gridwidth = 2;
		gbc.fill = GridBagConstraints.NONE;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(0, 10, 0, 0);
		panelCenter.add(this.selectFolderButton, gbc);

		gbc.gridx = 0;
		gbc.gridy = 6;
		gbc.gridwidth = 6;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.insets = new Insets(10, 0, 0, 0);
		panelCenter.add(this.downloadButton, gbc);

		gbc.gridx = 0;
		gbc.gridy = 7;
		gbc.gridwidth = 1;
		gbc.anchor = GridBagConstraints.WEST;
		panelCenter.add(this.progressLabel, gbc);

		gbc.gridx = 1;
		gbc.gridwidth = 5;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		this.progressBar.setStringPainted(true);
		panelCenter.add(this.progressBar, gbc);

		this.add(panelNorth, BorderLayout.NORTH);
		this.add(panelCenter, BorderLayout.CENTER);
	}

	private void createComponents() {
		this.queryLabel = new JLabel("Query ");
		this.scopusResultsLabel = new JLabel("Scopus ");
		this.pubMedResultsLabel = new JLabel("PubMed ");
		this.selectFolderLabel = new JLabel("Output folder ");
		this.downloadTypeLabel = new JLabel("Download");
		this.scopusLimitLabel = new JLabel("to");
		this.pubMedLimitLabel = new JLabel("to");
		this.progressLabel = new JLabel("Progress");

		this.queryTextField = new JTextField();
		this.selectFolderTextField = new JTextField();
		this.scopusResultsTextField = new JTextField();
		this.pubMedResultsTextField = new JTextField();
		this.scopusDownloadFromTextField = new JTextField("0", 6);
		this.pubMedDownloadFromTextField = new JTextField("0", 6);
		this.scopusDownloadToTextField = new JTextField("1000", 6);
		this.pubMedDownloadToTextField = new JTextField("1000", 6);

		this.searchButton = new JButton("Search");
		this.selectFolderButton = new JButton("Select...");
		this.downloadButton = new JButton("Download");

		this.abstractCheckBox = new JCheckBox("Abstract");
		this.fullTextCheckBox = new JCheckBox("Full text");
		this.scopusCheckBox = new JCheckBox("Scopus");
		this.pubMedCheckBox = new JCheckBox("PubMed");
		this.scopusLimitCheckBox = new JCheckBox("Download from");
		this.pubMedLimitCheckBox = new JCheckBox("Download from");
		this.convertPDFtoTXTCheckBox = new JCheckBox("Automatic PDF to text conversion");
		this.deletePDFCheckBox = new JCheckBox("Delete PDF");

		this.groupByPaperRadioButton = new JRadioButton("Group by paper");
		this.groupByTypeRadioButton = new JRadioButton("Group by abstract/full text");
		this.buttonGroup = new ButtonGroup();
		this.buttonGroup.add(this.groupByPaperRadioButton);
		this.buttonGroup.add(this.groupByTypeRadioButton);

		this.progressBar = new JProgressBar();
	}

}
