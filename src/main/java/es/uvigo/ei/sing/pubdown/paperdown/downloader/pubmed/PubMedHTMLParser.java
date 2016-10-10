package es.uvigo.ei.sing.pubdown.paperdown.downloader.pubmed;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.List;

import org.apache.http.HttpHost;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import es.uvigo.ei.sing.pubdown.paperdown.downloader.RepositoryManager;

public class PubMedHTMLParser {
	private static final String SUB_FOLDER_NAME = "pubmed_files";
	private static final String COMPLETE_PAPERS = SUB_FOLDER_NAME + File.separator + "complete_papers";
	private static final String ABSTRACT_PAPERS = SUB_FOLDER_NAME + File.separator + "abstract_papers";

	private static final String SEARCH_ID = "http://www.ncbi.nlm.nih.gov/pubmed/";

	private final CloseableHttpClient httpClient;
	private final HttpClientContext context;
	private List<String> idList;

	public PubMedHTMLParser(final CloseableHttpClient httpClient, final HttpClientContext context,
			final List<String> idList) {
		super();
		this.httpClient = httpClient;
		this.context = context;
		this.idList = idList;
	}

	public List<String> getIdList() {
		return idList;
	}

	public void setIdList(final List<String> idList) {
		this.idList = idList;
	}

	public void download(final String directory, final boolean isCompletePaper, final boolean convertPDFtoTXT,
			final boolean keepPDF, final boolean directoryType) {
		for (final String id : this.idList) {
			final String queryURL = SEARCH_ID + id;
			String paperTitle = "PUBMED_DEFAULT_NAME";
			try {
				Document document = Jsoup.connect(queryURL).get();

				final Elements titlesTexts = document.select("h1");
				for (final Element titleText : titlesTexts) {
					if (!titleText.text().equals("PubMed")) {
						paperTitle = titleText.text().replaceAll("[/|.]", "_");
						paperTitle = paperTitle.substring(0, paperTitle.length() - 1);
					}
				}
				if (isCompletePaper) {
					final Elements links = document.select("div.icons > a");
					final List<String> downloadedPDFList = new LinkedList<>();
					if (links != null) {
						String paperURL = links.attr("href");
						paperURL = redirectToURL(paperURL);
						document = Jsoup.connect(paperURL).get();
						final Elements pdfLinks = document.select("a");
						if (pdfLinks != null) {
							for (final Element link : pdfLinks) {
								final String href = link.absUrl("href");
								if (href.contains("pdf")) {
									paperURL = redirectToURL(href);
									if (!downloadedPDFList.contains(paperURL)) {
										downloadedPDFList.add(paperURL);
										final String directorySuffix = directoryType ? COMPLETE_PAPERS
												: SUB_FOLDER_NAME;

										RepositoryManager.generatePDFFile(paperURL, paperTitle, directory,
												directorySuffix, isCompletePaper, convertPDFtoTXT, keepPDF,
												directoryType);
									}
								}
							}
						}
					}
				} else {
					final Elements abstractTexts = document.select("div.abstr > div");
					if (abstractTexts != null) {
						final String htmlText = Jsoup.parse(abstractTexts.text()).text();
						if (!htmlText.isEmpty()) {
							final String directorySuffix = directoryType ? ABSTRACT_PAPERS : SUB_FOLDER_NAME;

							RepositoryManager.generateTXTFile(paperTitle, htmlText, directory, directorySuffix,
									isCompletePaper, directoryType);
						}
					}
				}
			} catch (final IOException e) {
				e.printStackTrace();
			}

		}
	}

	private String redirectToURL(final String url) throws IOException, ClientProtocolException {
		try {
			final HttpGet httpGet = new HttpGet(url);
			final CloseableHttpResponse response = this.httpClient.execute(httpGet, this.context);
			final HttpHost target = this.context.getTargetHost();
			final List<URI> redirectLocations = this.context.getRedirectLocations();
			final URI location = URIUtils.resolve(httpGet.getURI(), target, redirectLocations);
			response.close();
			return location.toString();
		} catch (final URISyntaxException e) {
			e.printStackTrace();
		}
		return null;
	}

}
