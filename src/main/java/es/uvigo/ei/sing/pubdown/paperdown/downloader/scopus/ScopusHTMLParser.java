package es.uvigo.ei.sing.pubdown.paperdown.downloader.scopus;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

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

	public ScopusHTMLParser(final CloseableHttpClient httpClient, final HttpClientContext context) {
		super();
		this.httpClient = httpClient;
		this.context = context;
	}

	public void downloadCompletePDFs(final Scopus scopus, final String directory, final boolean convertPDFtoTXT,
			final boolean keepPDF, final boolean directoryType) {
		try {
			final Document document = Jsoup.connect(scopus.getPaperUrl()).get();
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

				RepositoryManager.generatePDFFile(location.toASCIIString(), scopus.getPaperTitle(),
						scopus.getCompletePaperTitle(), directory, directorySuffix, scopus.isCompletePaper(),
						convertPDFtoTXT, keepPDF, directoryType);
			}
		} catch (URISyntaxException | IOException e) {
			e.printStackTrace();
		}
	}

	public void downloadAbstractTXTs(final Scopus scopus, final String directory, final boolean convertPDFtoTXT,
			final boolean keepPDF, final boolean directoryType) {
		final String directorySuffix = directoryType ? ABSTRACT_PAPERS : SUB_FOLDER_NAME;

		RepositoryManager.generatePDFFile(scopus.getPaperUrl(), scopus.getPaperTitle(), scopus.getCompletePaperTitle(),
				directory, directorySuffix, scopus.isCompletePaper(), convertPDFtoTXT, keepPDF, directoryType);
	}
}
