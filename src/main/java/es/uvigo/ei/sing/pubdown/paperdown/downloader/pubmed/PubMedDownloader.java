package es.uvigo.ei.sing.pubdown.paperdown.downloader.pubmed;

import static es.uvigo.ei.sing.pubdown.paperdown.downloader.RepositoryManager.numberOfPapersInRepository;

import java.io.IOException;
import java.io.StringReader;
import java.util.LinkedList;
import java.util.List;
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
	private String completePaperTitle = "";
	private String date = "";

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
			final boolean directoryType, final int downloadLimit, final int downloadFrom, int downloadTo) {

		List<PubMed> pubMedList = new LinkedList<>();
		int searchIncrease = 100;
		final int resultNumber = getResultSize();
		if (resultNumber > 0) {
			if (resultNumber < searchIncrease) {
				searchIncrease = resultNumber;
			}

			if (searchIncrease > downloadTo) {
				searchIncrease = downloadTo;
			}

			if (downloadLimit < resultNumber) {
				downloadTo = downloadLimit;
			}

			int aux = downloadTo;

			String queryURL = SEARCH_REQUEST + this.query.replace(" ", "+") + "&retmax=" + searchIncrease;
			final PubMedXMLParser xmlParser = new PubMedXMLParser(this.httpClient, this.context, queryURL);
			final PubMedHTMLParser htmlParser = new PubMedHTMLParser(this.httpClient, this.context);

			queryURL = queryURL + "&retstart=";

			for (int i = downloadFrom; i < downloadTo; i += searchIncrease) {
				xmlParser.setQueryURL(queryURL + i);

				if (aux < searchIncrease) {
					queryURL = SEARCH_REQUEST + this.query.replace(" ", "+") + "&retmax=" + aux + "&retstart=" + i;
					xmlParser.setQueryURL(queryURL);
				}

				final List<String> idList = xmlParser.getPubMedIDs();

				pubMedList.addAll(shouldDownloadPaper(idList, isCompletePaper));

				aux = aux - searchIncrease;
			}


			for (PubMed toDownload : pubMedList) {
				if (numberOfPapersInRepository(this.directory) < downloadLimit) {

					htmlParser.download(toDownload, this.directory, convertPDFtoTXT, keepPDF, directoryType);
					RepositoryManager.writeMetaData(this.directory, toDownload.getDoi(), toDownload.getPaperTitle(),
							toDownload.getCompletePaperTitle(), toDownload.getDate(), toDownload.getAuthorList(),
							toDownload.isCompletePaper());
				} else {
					break;
				}
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

	private List<PubMed> shouldDownloadPaper(final List<String> idList, final boolean isCompletePaper) {
		List<PubMed> pubMedList = new LinkedList<>();
		for (String pubMedId : idList) {
			final String queryURL = SEARCH_ID + pubMedId;
			final String queryURLXML = queryURL + SEARCH_XML;

			try {
				final org.jsoup.nodes.Document document = Jsoup.connect(queryURL).get();
				List<String> authorList = new LinkedList<>();
				String paperUrl = "";

				final Elements titlesTexts = document.select("h1");
				for (final Element titleText : titlesTexts) {
					if (!titleText.text().equals("PubMed")) {
						paperTitle = titleText.text().replaceAll("[/|.|;]", "_");
						completePaperTitle = paperTitle;
						if (paperTitle.length() > 130) {
							paperTitle = paperTitle.substring(0, 130);
						}
					}
				}

				final Elements authors = document.select("div.auths > a");
				for (final Element author : authors) {
					authorList.add(author.text());
				}

				final Elements url = document.select("div.icons > a");
				if (url != null) {
					paperUrl = url.attr("href");
				}

				date = getMetadataDate(queryURLXML);

				final Elements links = document.select("a");
				for (final Element link : links) {
					if (link.attr("ref").contains("aid_type=doi")) {
						doi = link.text();

						final Set<String> auxList = RepositoryManager.readDOIInMetaData(this.directory, doi);
						final String paperType = isCompletePaper ? "full" : "abstract";

						if (!auxList.contains(paperType)) {
							pubMedList.add(new PubMed(pubMedId, paperUrl, doi, paperTitle, completePaperTitle, date,
									authorList, isCompletePaper));
						}
					}
				}
			} catch (final IOException e) {
				e.printStackTrace();
			}
		}

		return pubMedList;
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

	// private boolean shouldDownloadPaper(final String id, final boolean
	// isCompletePaper) {
	// final String queryURL = SEARCH_ID + id;
	// final String queryURLXML = queryURL + SEARCH_XML;
	// try {
	// final org.jsoup.nodes.Document document = Jsoup.connect(queryURL).get();
	//
	// final Elements titlesTexts = document.select("h1");
	// for (final Element titleText : titlesTexts) {
	// if (!titleText.text().equals("PubMed")) {
	// paperTitle = titleText.text().replaceAll("[/|.]", "_");
	// completePaperTitle = paperTitle;
	// // paperTitle = paperTitle.substring(0, paperTitle.length()
	// // - 1);
	// if (paperTitle.length() > 130) {
	// paperTitle = paperTitle.substring(0, 130);
	// }
	// }
	// }
	//
	// final Elements authors = document.select("div.auths > a");
	// for (final Element author : authors) {
	// authorList.add(author.text());
	// }
	//
	// date = getMetadataDate(queryURLXML);
	//
	// final Elements links = document.select("a");
	// for (final Element link : links) {
	// if (link.attr("ref").contains("aid_type=doi")) {
	// doi = link.text();
	//
	// final Set<String> auxList =
	// RepositoryManager.readDOIInMetaData(this.directory, doi);
	// final String paperType = isCompletePaper ? "full" : "abstract";
	//
	// return !auxList.contains(paperType);
	// }
	// }
	// } catch (final IOException e) {
	// e.printStackTrace();
	// }
	//
	// return false;
	// }

}
