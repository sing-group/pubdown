package es.uvigo.ei.sing.pubdown.paperdown.downloader.scopus;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class ScopusXMLParser {
	private final CloseableHttpClient httpClient;
	private final HttpClientContext context;
	private String queryURL;

	public ScopusXMLParser(final CloseableHttpClient httpclient, final HttpClientContext context,
			final String queryURL) {
		super();
		this.httpClient = httpclient;
		this.context = context;
		this.queryURL = queryURL;
	}

	public String getQueryURL() {
		return queryURL;
	}

	public void setQueryURL(final String queryURL) {
		this.queryURL = queryURL;
	}

	public Map<String, String> getCompletePaperPDFURLs() {
		try {
			final Map<String, String> urlsWithTitle = new HashMap<>();
			final HttpGet httpget = new HttpGet(this.queryURL);
			final CloseableHttpResponse response = this.httpClient.execute(httpget, this.context);
			final String xmlDocument = EntityUtils.toString(response.getEntity());
			response.close();

			final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			final DocumentBuilder builder = factory.newDocumentBuilder();
			final Document document = builder.parse(new InputSource(new StringReader(xmlDocument)));
			document.getDocumentElement().normalize();
			final NodeList errorElements = document.getElementsByTagName("error");
			if (errorElements.item(0) != null) {
				return null;
			} else {
				final NodeList entryElements = document.getElementsByTagName("entry");
				String paperURL = "";
				String paperTitle = "";
				for (int i = 0; i < entryElements.getLength(); i++) {

					final NodeList entryChildrens = entryElements.item(i).getChildNodes();

					for (int j = 0; j < entryChildrens.getLength(); j++) {
						final Node child = entryChildrens.item(j);
						if (child.getNodeName().equals("link")) {
							final Attr attribute = (Attr) child.getAttributes().getNamedItem("ref");
							if (attribute.getNodeValue().equals("scidir")) {
								paperURL = child.getAttributes().getNamedItem("href").getTextContent();
							}
						}
						if (child.getNodeName().equals("dc:title")) {
							paperTitle = child.getFirstChild().getTextContent();
							// if (paperTitle.contains(";")) {
							// paperTitle = paperTitle.replace(";", " - ");
							// }
							// if (paperTitle.contains("/")) {
							// paperTitle = paperTitle.replace("/", " - ");
							// }
							// if (paperTitle.contains(".")) {
							// paperTitle = paperTitle.replace(".", " - ");
							// }

							paperTitle = paperTitle.replaceAll("[/|.|;]", " - ");
							if (paperTitle.length() > 130) {
								paperTitle = paperTitle.substring(0, 130);
							}
						}
					}
					urlsWithTitle.put(paperURL, paperTitle);
				}
				return urlsWithTitle;
			}
		} catch (IOException | SAXException | ParserConfigurationException e) {
			e.printStackTrace();
		}
		return null;
	}

	public Map<String, String> getAbstractPaperPDFURLs(final String apiKey) {
		try {
			final Map<String, String> urlsWithTitle = new HashMap<>();
			final HttpGet httpget = new HttpGet(this.queryURL);
			final CloseableHttpResponse response = this.httpClient.execute(httpget, this.context);
			final String xmlDocument = EntityUtils.toString(response.getEntity());
			response.close();

			final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			final DocumentBuilder builder = factory.newDocumentBuilder();
			final Document document = builder.parse(new InputSource(new StringReader(xmlDocument)));
			document.getDocumentElement().normalize();
			final NodeList errorElements = document.getElementsByTagName("error");
			if (errorElements.item(0) != null) {
				return null;
			} else {
				final NodeList entryElements = document.getElementsByTagName("entry");
				String paperURL = "";
				String paperTitle = "";
				for (int i = 0; i < entryElements.getLength(); i++) {

					final NodeList entryChildrens = entryElements.item(i).getChildNodes();

					for (int j = 0; j < entryChildrens.getLength(); j++) {
						final Node child = entryChildrens.item(j);
						if (child.getNodeName().equals("prism:doi")) {
							paperURL = "http://api.elsevier.com/content/article/doi/"
									+ child.getFirstChild().getTextContent() + "?httpAccept=application/pdf&apiKey="
									+ apiKey;
						}
						if (child.getNodeName().equals("dc:title")) {
							paperTitle = child.getFirstChild().getTextContent();
							if (paperTitle.contains(";")) {
								paperTitle = paperTitle.replace(";", " - ");
							}
							if (paperTitle.contains("/")) {
								paperTitle = paperTitle.replace("/", " - ");
							}
							if (paperTitle.contains(".")) {
								paperTitle = paperTitle.replace(".", " - ");
							}
							if (paperTitle.length() > 130) {
								paperTitle = paperTitle.substring(0, 130);
							}
						}
					}
					urlsWithTitle.put(paperURL, paperTitle);
				}
				return urlsWithTitle;
			}
		} catch (IOException | SAXException | ParserConfigurationException e) {
			e.printStackTrace();
		}
		return null;
	}
}
