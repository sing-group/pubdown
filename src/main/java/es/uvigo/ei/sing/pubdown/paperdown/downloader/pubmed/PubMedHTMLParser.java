package es.uvigo.ei.sing.pubdown.paperdown.downloader.pubmed;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
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

	public PubMedHTMLParser(final CloseableHttpClient httpClient, final HttpClientContext context) {
		super();
		this.httpClient = httpClient;
		this.context = context;
	}

	public void download(final PubMed pubmed, final String directory, final boolean convertPDFtoTXT,
			final boolean keepPDF, final boolean directoryType) {
		try {
			if (pubmed.isCompletePaper()) {
				String paperURL = pubmed.getPaperUrl();
				paperURL = redirectToURL(paperURL);
				Document document = Jsoup.connect(paperURL).get();
				final Elements pdfLinks = document.select("a");
				if (pdfLinks != null) {
					for (final Element link : pdfLinks) {
						final String href = link.absUrl("href");
						if (href.contains("pdf")) {
							paperURL = redirectToURL(href);
							final String directorySuffix = directoryType ? COMPLETE_PAPERS : SUB_FOLDER_NAME;

							RepositoryManager.generatePDFFile(paperURL, pubmed.getPaperTitle(),
									pubmed.getCompletePaperTitle(), directory, directorySuffix,
									pubmed.isCompletePaper(), convertPDFtoTXT, keepPDF, directoryType);
						}
					}
				}
			} else {
				Document document = Jsoup.connect(SEARCH_ID + pubmed.getId()).get();
				final Elements abstractTexts = document.select("div.abstr > div");
				if (abstractTexts != null) {
					final String htmlText = Jsoup.parse(abstractTexts.text()).text();
					if (!htmlText.isEmpty()) {
						final String directorySuffix = directoryType ? ABSTRACT_PAPERS : SUB_FOLDER_NAME;

						RepositoryManager.generateTXTFile(pubmed.getPaperTitle(), pubmed.getCompletePaperTitle(),
								htmlText, directory, directorySuffix, pubmed.isCompletePaper(), directoryType);
					}
				}
			}
		} catch (final IOException e) {
			e.printStackTrace();
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
