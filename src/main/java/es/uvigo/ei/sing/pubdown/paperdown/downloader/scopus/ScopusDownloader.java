package es.uvigo.ei.sing.pubdown.paperdown.downloader.scopus;

import java.io.IOException;
import java.io.StringReader;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import es.uvigo.ei.sing.pubdown.paperdown.downloader.DownloadEvent;
import es.uvigo.ei.sing.pubdown.paperdown.downloader.DownloadListener;
import es.uvigo.ei.sing.pubdown.paperdown.downloader.RepositoryManager;
import es.uvigo.ei.sing.pubdown.paperdown.downloader.Searcher;
import es.uvigo.ei.sing.pubdown.web.entities.RepositoryQuery;

public class ScopusDownloader implements Searcher {
	private static final String SEARCH_REQUEST = "https://api.elsevier.com/content/search/scidir?";
	private static final String SEARCH_REQUEST_TYPE = "xml";
	private static final String SEARCH_START = "0";

	private CloseableHttpClient httpClient;
	private HttpClientContext context;
	private RepositoryQuery repositoryQuery;
	private String query;
	private String apiKey;
	private String directory;
	private final List<DownloadListener> downloadListeners = new CopyOnWriteArrayList<>();

	private String doi = "";
	private String paperTitle = "";
	private String date = "";
	private final List<String> authorList = new LinkedList<>();

	public ScopusDownloader() {

	}

	public ScopusDownloader(final String query, final String apiKey, final String directory) {
		super();
		this.query = query;
		this.apiKey = apiKey;
		this.directory = directory;
		final RequestConfig globalConfig = RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD).build();
		this.httpClient = HttpClients.custom().setDefaultRequestConfig(globalConfig).build();
		this.context = HttpClientContext.create();
	}

	public RepositoryQuery getRepositoryQuery() {
		return repositoryQuery;
	}

	public String getDirectory() {
		return directory;
	}

	public void setDirectory(final String directory) {
		this.directory = directory;
	}

	@Override
	public void downloadPapers(final boolean isCompletePaper, final boolean convertPDFtoTXT, final boolean keepPDF,
			final boolean directoryType, final int downloadLimit, final int downloadFrom, int downloadTo) {

		int searchIncrease = 1;
		final int resultNumber = getResultSize();
		if (resultNumber > 0) {

			if (resultNumber < searchIncrease) {
				searchIncrease = resultNumber;
			}

			if (searchIncrease > downloadTo) {
				searchIncrease = downloadTo;
			}

			if (downloadLimit <= resultNumber) {
				downloadTo = downloadLimit;
			}

			int aux = downloadTo;

			String queryURL = SEARCH_REQUEST + "count=" + searchIncrease + "&query=" + this.query + "&apiKey="
					+ this.apiKey + "&httpAccept=application%2F" + SEARCH_REQUEST_TYPE + "&start=";

			final ScopusXMLParser xmlParser = new ScopusXMLParser(this.httpClient, this.context, queryURL);
			final ScopusHTMLParser htmlParser = new ScopusHTMLParser(this.httpClient, this.context, null);

			for (int i = downloadFrom; i < downloadTo; i += searchIncrease) {

				xmlParser.setQueryURL(queryURL + i);

				if (aux < searchIncrease) {
					queryURL = SEARCH_REQUEST + "count=" + aux + "&query=" + this.query + "&apiKey=" + this.apiKey
							+ "&httpAccept=application%2F" + SEARCH_REQUEST_TYPE + "&start=" + i;
					xmlParser.setQueryURL(queryURL);
				}

				final Map<String, String> urlsWithTitle = isCompletePaper ? xmlParser.getCompletePaperPDFURLs()
						: xmlParser.getAbstractPaperPDFURLs(this.apiKey);

				final int numberOfPapers = (int) RepositoryManager.numberOfPapersInRepository(this.directory);

				if (shouldDownloadPaper(xmlParser.getQueryURL(), isCompletePaper)) {
					if ((numberOfPapers < downloadLimit)) {
						htmlParser.setUrlsWithTitle(urlsWithTitle);
						downloadCompleteOrAbstract(isCompletePaper, htmlParser, convertPDFtoTXT, keepPDF,
								directoryType);
						RepositoryManager.writeMetaData(this.directory, doi, paperTitle, date, authorList,
								isCompletePaper);
						authorList.clear();
					}
				}

				aux = aux - searchIncrease;

			}
		}
	}

	@Override
	public int getResultSize() {
		try {
			final String queryURL = (SEARCH_REQUEST + "start=" + SEARCH_START + "&count=" + 1 + "&" + "query="
					+ this.query + "&apiKey=" + this.apiKey + "&httpAccept=application%2F" + SEARCH_REQUEST_TYPE);
			final HttpGet httpget = new HttpGet(queryURL);
			final CloseableHttpResponse response = this.httpClient.execute(httpget, this.context);
			final String xmlDocument = EntityUtils.toString(response.getEntity());
			response.close();

			final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			final DocumentBuilder builder = factory.newDocumentBuilder();
			final Document document = builder.parse(new InputSource(new StringReader(xmlDocument)));
			document.getDocumentElement().normalize();
			final NodeList errorElements = document.getElementsByTagName("error");

			if (errorElements.item(0) != null) {
				return 0;
			} else {
				final NodeList statusCodeElements = document.getElementsByTagName("statusCode");
				if (statusCodeElements.item(0) != null) {
					return 0;
				} else {
					final NodeList entryElements = document.getElementsByTagName("opensearch:totalResults");
					return Integer.parseInt(entryElements.item(0).getFirstChild().getNodeValue());
				}
			}
		} catch (IOException | SAXException | ParserConfigurationException e) {
			e.printStackTrace();
		}
		return 0;
	}

	@Override
	public void addDownloadListener(final DownloadListener downloadEvent) {
		if (!this.downloadListeners.contains(downloadEvent)) {
			this.downloadListeners.add(downloadEvent);
		}
	}

	@Override
	public void removeDownloadListener(final DownloadListener downloadListener) {
		if (this.downloadListeners.contains(downloadListener)) {
			this.downloadListeners.remove(downloadListener);
		}
	}

	@Override
	public boolean containsDownloadListener(final DownloadListener downloadListener) {
		return this.downloadListeners.contains(downloadListener);
	}

	public void notifyDownloadListeners(final DownloadEvent downloadEvent) {
		for (final DownloadListener downloadListener : downloadListeners) {
			downloadListener.downloadComplete(downloadEvent);
		}
	}

	@Override
	public void removeAllDownloadListeners() {
		this.downloadListeners.clear();
	}

	private boolean shouldDownloadPaper(final String query, final boolean isCompletePaper) {
		final HttpGet httpget = new HttpGet(query);
		try (final CloseableHttpResponse response = this.httpClient.execute(httpget, this.context)) {
			final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			final DocumentBuilder builder = factory.newDocumentBuilder();
			final Document document = builder.parse(new InputSource(response.getEntity().getContent()));
			document.getDocumentElement().normalize();
			final NodeList errorElements = document.getElementsByTagName("error");
			if (errorElements.item(0) != null) {
			} else {
				final NodeList entryElements = document.getElementsByTagName("entry");
				for (int i = 0; i < entryElements.getLength(); i++) {
					final NodeList entryChildrens = entryElements.item(i).getChildNodes();

					for (int j = 0; j < entryChildrens.getLength(); j++) {
						final Node child = entryChildrens.item(j);
						if (child.getNodeName().equals("authors")) {
							final NodeList authorsChildren = child.getChildNodes();
							for (int k = 0; k < authorsChildren.getLength(); k++) {
								final NodeList author = authorsChildren.item(k).getChildNodes();
								if (!author.item(0).getTextContent().equalsIgnoreCase("NA")) {
									final String authorName = author.item(0).getTextContent();
									final String authorSurName = author.item(1).getTextContent();
									authorList.add(authorSurName + " " + authorName);
								}
							}
						}

						if (child.getNodeName().equals("prism:coverDate")) {
							date = child.getFirstChild().getTextContent();
							if (date.contains(";")) {
								date = date.replace(";", "-");
							}
						}
					}

					for (int j = 0; j < entryChildrens.getLength(); j++) {
						final Node child = entryChildrens.item(j);

						if (child.getNodeName().equals("dc:title")) {
							paperTitle = child.getFirstChild().getTextContent();
							if (paperTitle.contains(";")) {
								paperTitle = paperTitle.replace(";", " - ");
							}
							if (paperTitle.length() > 130) {
								paperTitle = paperTitle.substring(0, 130);
							}
						}

						if (child.getNodeName().equals("prism:doi")) {
							doi = child.getFirstChild().getTextContent();

							final Set<String> auxList = RepositoryManager.readDOIInMetaData(this.directory, doi);
							final String paperType = isCompletePaper ? "full" : "abstract";

							return !auxList.contains(paperType);
						}
					}
				}
			}
		} catch (IOException | SAXException | ParserConfigurationException e) {
			e.printStackTrace();
		}
		return false;
	}

	private void downloadCompleteOrAbstract(final boolean isCompletePaper, final ScopusHTMLParser htmlParser,
			final boolean convertPDFtoTXT, final boolean keepPDF, final boolean directoryType) {
		if (isCompletePaper) {
			htmlParser.downloadCompletePDFs(this.directory, isCompletePaper, convertPDFtoTXT, keepPDF, directoryType);
		} else {
			htmlParser.downloadAbstractTXTs(this.directory, isCompletePaper, convertPDFtoTXT, keepPDF, directoryType);
		}
	}

}
