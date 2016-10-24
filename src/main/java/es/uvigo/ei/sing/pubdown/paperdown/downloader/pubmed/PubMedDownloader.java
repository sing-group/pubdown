package es.uvigo.ei.sing.pubdown.paperdown.downloader.pubmed;

import java.io.IOException;
import java.io.StringReader;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import es.uvigo.ei.sing.pubdown.paperdown.downloader.DownloadEvent;
import es.uvigo.ei.sing.pubdown.paperdown.downloader.DownloadListener;
import es.uvigo.ei.sing.pubdown.paperdown.downloader.RepositoryManager;
import es.uvigo.ei.sing.pubdown.paperdown.downloader.Searcher;

public class PubMedDownloader implements Searcher {
	private static final String SEARCH_REQUEST = "http://eutils.ncbi.nlm.nih.gov/entrez/eutils/esearch.fcgi?term=";
	private static final String SEARCH_ID = "http://www.ncbi.nlm.nih.gov/pubmed/";
	private static final String SEARCH_XML = "?report=medline&format=text";

	private CloseableHttpClient httpClient;
	private HttpClientContext context;
	private String directory;
	private String query;
	private final List<DownloadListener> downloadListeners = new CopyOnWriteArrayList<>();

	private String doi = "";
	private String paperTitle = "";
	private String date = "";
	private final List<String> authorList = new LinkedList<>();

	public PubMedDownloader() {
	}

	public PubMedDownloader(final String query, final String directory) {
		this.query = query;
		this.directory = directory;
		final RequestConfig globalConfig = RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD).build();
		this.httpClient = HttpClients.custom().setDefaultRequestConfig(globalConfig).build();
		this.context = HttpClientContext.create();
	}

	public String getDirectory() {
		return directory;
	}

	public void setDirectory(final String directory) {
		this.directory = directory;
	}

	@Override
	public void downloadPapers(final boolean isCompletePaper, final boolean convertPDFtoTXT, final boolean keepPDF,
			final boolean directoryType, final int downloadFrom, final int downloadTo) {

		int aux = downloadTo;
		int searchIncrease = 1;
		final int resultNumber = getResultSize();
		if (resultNumber > 0) {
			if (resultNumber < searchIncrease) {
				searchIncrease = resultNumber;
			}
			if (searchIncrease > downloadTo) {
				searchIncrease = downloadTo;
			}
			String queryURL = SEARCH_REQUEST + this.query.replace(" ", "+") + "&retmax=" + searchIncrease;
			final PubMedXMLParser xmlParser = new PubMedXMLParser(this.httpClient, this.context, queryURL);
			final PubMedHTMLParser htmlParser = new PubMedHTMLParser(this.httpClient, this.context, null);

			queryURL = queryURL + "&retstart=";

			for (int i = downloadFrom; i < downloadTo; i += searchIncrease) {
				xmlParser.setQueryURL(queryURL + i);

				if (aux < searchIncrease) {
					queryURL = SEARCH_REQUEST + this.query.replace(" ", "+") + "&retmax=" + aux + "&retstart=" + i;
					xmlParser.setQueryURL(queryURL);
				}

				final List<String> idList = xmlParser.getPubMedIDs();

				for (final String id : idList) {
					if (checkMetadata(id, isCompletePaper)) {
						htmlParser.setIdList(idList);
						htmlParser.download(this.directory, isCompletePaper, convertPDFtoTXT, keepPDF, directoryType);
						RepositoryManager.writeMetaData(this.directory, doi, paperTitle, date, authorList,
								isCompletePaper);
					}
				}

				aux = aux - searchIncrease;

				notifyDownloadListeners(new DownloadEvent());
			}
		}
	}

	@Override
	public int getResultSize() {
		try {
			final String queryURL = SEARCH_REQUEST + this.query.replace(" ", "+");
			final HttpGet httpGet = new HttpGet(queryURL);
			final CloseableHttpResponse response = this.httpClient.execute(httpGet, this.context);
			final String xmlDocument = EntityUtils.toString(response.getEntity());
			response.close();
			final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			final DocumentBuilder builder = factory.newDocumentBuilder();
			final Document document = builder.parse(new InputSource(new StringReader(xmlDocument)));
			document.getDocumentElement().normalize();
			final NodeList countElements = document.getElementsByTagName("Count");
			if (countElements.item(0) != null) {
				return Integer.parseInt(countElements.item(0).getFirstChild().getNodeValue());
			}
		} catch (IOException | SAXException | ParserConfigurationException e) {
			e.printStackTrace();
		}
		return 0;
	}

	@Override
	public void addDownloadListener(final DownloadListener downloadListener) {
		if (!this.downloadListeners.contains(downloadListener)) {
			this.downloadListeners.add(downloadListener);
		}
	}

	@Override
	public void removeDownloadListener(final DownloadListener downloadListener) {
		if (this.downloadListeners.contains(downloadListener)) {
			this.downloadListeners.remove(downloadListener);
		}
	}

	@Override
	public void removeAllDownloadListeners() {
		this.downloadListeners.clear();
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

	private boolean checkMetadata(final String id, final boolean isCompletePaper) {
		final String queryURL = SEARCH_ID + id;
		final String queryURLXML = queryURL + SEARCH_XML;
		try {
			final org.jsoup.nodes.Document document = Jsoup.connect(queryURL).get();

			final Elements titlesTexts = document.select("h1");
			for (final Element titleText : titlesTexts) {
				if (!titleText.text().equals("PubMed")) {
					paperTitle = titleText.text().replaceAll("[/|.]", "_");
					paperTitle = paperTitle.substring(0, paperTitle.length() - 1);
				}
			}

			final Elements authors = document.select("div.auths > a");
			for (final Element author : authors) {
				authorList.add(author.text());
			}

			date = getMetadataDate(queryURLXML);

			final Elements links = document.select("a");
			for (final Element link : links) {
				if (link.attr("ref").contains("aid_type=doi")) {
					doi = link.text();
					final Map<String, String> doiMap = RepositoryManager.readMetaData(this.directory);
					if (!doiMap.containsKey(doi)) {
						return true;
					} else {
						final Map<String, List<String>> auxMap = RepositoryManager.readDOIInMetaData(this.directory,
								doi);
						final List<String> auxList = auxMap.get(doi);
						if (auxList.size() == 1) {
							final String type = auxList.get(0);
							final String paperType = isCompletePaper ? "full" : "abstract";
							if (!paperType.equals(type)) {
								return true;
							}
						}
					}
				}
			}
		} catch (final IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	private String getMetadataDate(final String queryURL) {
		try {
			final org.jsoup.nodes.Document document = Jsoup.connect(queryURL).get();
			String date = document.text();
			date = date.substring(date.indexOf("DA"), date.indexOf("IS"));
			if (date.contains("LR")) {
				date = date.substring(date.indexOf("DA"), date.indexOf("LR"));
			}
			final String[] dateFilter = date.split("-");
			date = dateFilter[1];
			if (date.contains("\n")) {
				date = date.replace("\n", "");
			}
			date = date.substring(1);
			final String year = date.substring(0, 4);
			final String month = date.substring(4, 6);
			final String day = date.substring(6, 8);
			date = year + "-" + month + "-" + day;
			return date;

		} catch (final IOException e) {
		}
		return null;
	}
}
