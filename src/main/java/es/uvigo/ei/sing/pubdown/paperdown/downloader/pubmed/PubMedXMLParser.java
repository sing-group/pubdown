package es.uvigo.ei.sing.pubdown.paperdown.downloader.pubmed;

import java.io.IOException;
import java.io.StringReader;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class PubMedXMLParser {
	private final CloseableHttpClient httpClient;
	private final HttpClientContext context;
	private String queryURL;

	public PubMedXMLParser(CloseableHttpClient httpclient, HttpClientContext context, String queryURL) {
		super();
		this.httpClient = httpclient;
		this.context = context;
		this.queryURL = queryURL;
	}

	public String getQueryURL() {
		return queryURL;
	}

	public void setQueryURL(String queryURL) {
		this.queryURL = queryURL;
	}

	public List<String> getPubMedIDs() {
		try {
			final List<String> idList = new LinkedList<>();
			final HttpGet httpGet = new HttpGet(this.queryURL);
			final CloseableHttpResponse response = this.httpClient.execute(httpGet, this.context);
			final String xmlDocument = EntityUtils.toString(response.getEntity());
			response.close();

			final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			final DocumentBuilder builder = factory.newDocumentBuilder();
			final Document document = builder.parse(new InputSource(new StringReader(xmlDocument)));
			document.getDocumentElement().normalize();
			final NodeList errorElements = document.getElementsByTagName("ErrorList");
			if (errorElements.item(0) != null) {
				return null;
			} else {
				final NodeList entryElements = document.getElementsByTagName("Id");
				for (int i = 0; i < entryElements.getLength(); i++) {
					final Node child = entryElements.item(i);
					idList.add(child.getFirstChild().getTextContent());
				}
				return idList;
			}
		} catch (IOException | SAXException | ParserConfigurationException e) {
			e.printStackTrace();
		}
		return null;
	}
}
