package es.uvigo.ei.sing.pubdown.paperdown.downloader.scopus;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpHost;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import es.uvigo.ei.sing.pubdown.paperdown.downloader.RepositoryManager;

public class ScopusHTMLParser {
	private static final String SUB_FOLDER_NAME = "scopus_files";
	private static final String COMPLETE_PAPERS = SUB_FOLDER_NAME + File.separator + "complete_papers";
	private static final String ABSTRACT_PAPERS = SUB_FOLDER_NAME + File.separator + "abstract_papers";

	private final CloseableHttpClient httpClient;
	private final HttpClientContext context;
	private Map<String, String> urlsWithTitle;

	public ScopusHTMLParser(CloseableHttpClient httpClient, HttpClientContext context,
			Map<String, String> urlsWithTitle) {
		super();
		this.httpClient = httpClient;
		this.context = context;
		this.urlsWithTitle = urlsWithTitle;
	}

	public Map<String, String> getUrlsWithTitle() {
		return urlsWithTitle;
	}

	public void setUrlsWithTitle(Map<String, String> urlsWithTitle) {
		this.urlsWithTitle = urlsWithTitle;
	}

	public void downloadCompletePDFs(String directory, boolean isCompletePaper, boolean convertPDFtoTXT,
			boolean keepPDF, boolean directoryType) {
		this.urlsWithTitle.forEach((url, fileName) -> {
			try {
				final Document document = Jsoup.connect(url).get();
				final Elements link = document.select("#pdfLink");
				final String redirectLink = link.attr("pdfurl");
				if (!redirectLink.isEmpty()) {
					final HttpGet httpGet = new HttpGet(redirectLink);
					final CloseableHttpResponse response = this.httpClient.execute(httpGet, context);
					final HttpHost target = context.getTargetHost();
					final List<URI> redirectLocations = context.getRedirectLocations();
					final URI location = URIUtils.resolve(httpGet.getURI(), target, redirectLocations);
					response.close();
					final String directorySuffix = directoryType ? COMPLETE_PAPERS : SUB_FOLDER_NAME;

					RepositoryManager.generatePDFFile(location.toASCIIString(), fileName, directory, directorySuffix,
							isCompletePaper, convertPDFtoTXT, keepPDF, directoryType);
				}
			} catch (URISyntaxException | IOException e) {
				e.printStackTrace();
			}
		});
	}

	public void downloadAbstractTXTs(String directory, boolean isCompletePaper, boolean convertPDFtoTXT,
			boolean keepPDF, boolean directoryType) {
		this.urlsWithTitle.forEach((url, fileName) -> {
			final String directorySuffix = directoryType ? ABSTRACT_PAPERS : SUB_FOLDER_NAME;

			RepositoryManager.generatePDFFile(url, fileName, directory, directorySuffix, isCompletePaper,
					convertPDFtoTXT, keepPDF, directoryType);
		});
	}
}
