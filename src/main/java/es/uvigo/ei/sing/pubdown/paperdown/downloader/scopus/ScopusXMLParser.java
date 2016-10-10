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
	private String selfLink;
	private String lastLink;
	private String nextLink;

	public ScopusXMLParser(final CloseableHttpClient httpclient, final HttpClientContext context,
			final String queryURL) {
		super();
		this.httpClient = httpclient;
		this.context = context;
		this.queryURL = queryURL;
		this.selfLink = "";
		this.nextLink = "";
		this.lastLink = "";
	}

	public String getSelfLink() {
		return selfLink;
	}

	public void setSelfLink(final String selfLink) {
		this.selfLink = selfLink;
	}

	public String getLastLink() {
		return lastLink;
	}

	public void setLastLink(final String lastLink) {
		this.lastLink = lastLink;
	}

	public String getNextLink() {
		return nextLink;
	}

	public void setNextLink(final String nextLink) {
		this.nextLink = nextLink;
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
							if (paperTitle.contains(";")) {
								paperTitle = paperTitle.replace(";", " - ");
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

	public void updateXMLURLs() {
		try {
			final HttpGet httpget = new HttpGet(this.queryURL);
			final CloseableHttpResponse response = this.httpClient.execute(httpget, this.context);
			final String xmlDocument = EntityUtils.toString(response.getEntity());
			response.close();

			final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			final DocumentBuilder builder = factory.newDocumentBuilder();
			final Document document = builder.parse(new InputSource(new StringReader(xmlDocument)));
			document.getDocumentElement().normalize();
			final NodeList linkElements = document.getElementsByTagName("link");
			for (int i = 0; i < linkElements.getLength(); i++) {
				final String ref = linkElements.item(i).getAttributes().getNamedItem("ref").getNodeValue().toString();
				if (linkElements.item(i).getAttributes().getNamedItem("type") != null) {
					switch (ref) {
					case "self":
						this.setSelfLink(
								(linkElements.item(i).getAttributes().getNamedItem("href").getNodeValue().toString())
										.replace("https://api.elsevier.com:80", "https://api.elsevier.com")
										.replace("&xml-decode=true", ""));
						break;
					case "next":
						this.setNextLink(
								(linkElements.item(i).getAttributes().getNamedItem("href").getNodeValue().toString())
										.replace("https://api.elsevier.com:80", "https://api.elsevier.com")
										.replace("&xml-decode=true", ""));
						break;
					case "last":
						this.setLastLink(
								(linkElements.item(i).getAttributes().getNamedItem("href").getNodeValue().toString())
										.replace("https://api.elsevier.com:80", "https://api.elsevier.com")
										.replace("&xml-decode=true", ""));
						break;
					default:
						break;
					}
				}
			}
		} catch (IOException | SAXException | ParserConfigurationException e) {
			e.printStackTrace();
		}
	}
}
