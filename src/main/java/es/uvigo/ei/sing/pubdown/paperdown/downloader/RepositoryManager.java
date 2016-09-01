package es.uvigo.ei.sing.pubdown.paperdown.downloader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.io.RandomAccessFile;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

public class RepositoryManager {

	private static final String PDF_FILE_EXTENSION = ".pdf";
	private static final String TXT_FILE_EXTENSION = ".txt";
	private static final String LOG_FILE = "log.csv";

	private static final String SEMICOLON_DELIMITER = ";";
	private static final String METADATA_FILE = "metadata.csv";

	public static void generatePDFFile(String pdfURL, String fileName, String directory, String directorySuffix,
			boolean isCompletePaper, boolean convertPDFtoTXT, boolean keepPDF, boolean directoryType) {
		try {
			final URL url = new URL(pdfURL);
			final ReadableByteChannel readableByteChannel = Channels.newChannel(url.openStream());
			File file;
			String createdFile;
			File subDirectory;
			if (directoryType) {
				subDirectory = createDirectory(directory + directorySuffix);
				createdFile = subDirectory.getAbsolutePath() + File.separator + fileName;
				file = new File(createdFile + PDF_FILE_EXTENSION);
			} else {
				final String pdfSubFolder = isCompletePaper ? "complete" : "abstract";
				subDirectory = createDirectory(
						directory + directorySuffix + File.separator + fileName + File.separator + pdfSubFolder);
				createdFile = subDirectory.getAbsolutePath() + File.separator + fileName;
				file = new File(createdFile + PDF_FILE_EXTENSION);
			}

			FileOutputStream fileOutputStream;
			if (!file.exists()) {
				fileOutputStream = new FileOutputStream(createdFile + PDF_FILE_EXTENSION);
			} else {
				final UUID randomUUID = UUID.randomUUID();
				fileOutputStream = new FileOutputStream(createdFile + "_" + randomUUID + PDF_FILE_EXTENSION);
				file = new File(createdFile + "_" + randomUUID + PDF_FILE_EXTENSION);
			}
			fileOutputStream.getChannel().transferFrom(readableByteChannel, 0, Long.MAX_VALUE);
			fileOutputStream.close();

			if (!checkIfCorruptedPDF(file)) {
				if (convertPDFtoTXT) {
					convertPDFFiletoTXTFile(file, directory, directorySuffix, isCompletePaper, keepPDF, directoryType);
				} else {
					try (FileWriter logFile = new FileWriter(directory + File.separator + LOG_FILE, true)) {
						final String paperType = isCompletePaper ? "full;" : "abstract;";
						logFile.write(paperType + file.getName() + ";OK;\n");
					} catch (final IOException e) {
					}

				}
			} else {
				try (FileWriter logFile = new FileWriter(directory + File.separator + LOG_FILE, true)) {
					final String paperType = isCompletePaper ? "full;" : "abstract;";
					logFile.write(paperType + file.getName() + ";error;\n");
				} catch (final IOException e) {
				}
			}

		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	public static void generateTXTFile(String fileName, String abstractText, String directory, String directorySuffix,
			boolean isCompletePaper, boolean directoryType) {
		File file;
		String createdFile;
		File subDirectory;
		if (directoryType) {
			subDirectory = createDirectory(directory + directorySuffix);
			createdFile = subDirectory.getAbsolutePath() + File.separator + fileName;
			file = new File(createdFile + TXT_FILE_EXTENSION);
		} else {
			final String pdfSubFolder = isCompletePaper ? "complete" : "abstract";
			subDirectory = createDirectory(
					directory + directorySuffix + File.separator + fileName + File.separator + pdfSubFolder);
			createdFile = subDirectory.getAbsolutePath() + File.separator + fileName;
			file = new File(createdFile + TXT_FILE_EXTENSION);
		}
		if (file.exists()) {
			file = new File(createdFile + "_" + UUID.randomUUID() + TXT_FILE_EXTENSION);
		}
		try (PrintWriter printWriter = new PrintWriter(file)) {
			printWriter.println(abstractText);
		} catch (final FileNotFoundException e) {
			e.printStackTrace();
		}
		try (FileWriter logFile = new FileWriter(directory + File.separator + LOG_FILE, true)) {
			final String paperType = isCompletePaper ? "full;" : "abstract;";
			logFile.write(paperType + file.getName() + ";OK;\n");
		} catch (final IOException e) {
		}

	}

	private static void convertPDFFiletoTXTFile(File file, String directory, String directorySuffix,
			boolean isCompletePaper, boolean keepPDF, boolean directoryType) {
		PDFTextStripper pdfStripper = null;
		PDDocument pdDoc = null;
		COSDocument cosDoc = null;
		try {
			final PDFParser parser = new PDFParser(new RandomAccessFile(file, "r"));
			parser.parse();
			cosDoc = parser.getDocument();
			pdfStripper = new PDFTextStripper();
			pdDoc = new PDDocument(cosDoc);
			pdfStripper.setEndPage(pdDoc.getNumberOfPages());
			final String parsedText = pdfStripper.getText(pdDoc);
			pdDoc.close();
			generateTXTFile(file.getName().replace(PDF_FILE_EXTENSION, ""), parsedText, directory, directorySuffix,
					isCompletePaper, directoryType);
			if (!keepPDF) {
				file.delete();
			}
		} catch (final IOException e) {
			if (file.delete()) {
				try (FileWriter logFile = new FileWriter(directory + File.separator + LOG_FILE, true)) {
					final String paperType = isCompletePaper ? "full;" : "abstract;";
					logFile.write(paperType + file.getName() + ";error;\n");
				} catch (final IOException e1) {
				}
			}
		}
	}

	private static boolean checkIfCorruptedPDF(File file) {
		PDFTextStripper pdfStripper = null;
		PDDocument pdDoc = null;
		COSDocument cosDoc = null;
		try {
			final PDFParser parser = new PDFParser(new RandomAccessFile(file, "r"));
			parser.parse();
			cosDoc = parser.getDocument();
			pdfStripper = new PDFTextStripper();
			pdDoc = new PDDocument(cosDoc);
			pdfStripper.setEndPage(pdDoc.getNumberOfPages());
			pdDoc.close();
			return false;
		} catch (final Exception e) {
			if (file.delete()) {
				return true;
			}
		}
		return false;
	}

	private static File createDirectory(String directory) {
		final File newDirectory = new File(directory);
		if (!newDirectory.exists()) {
			newDirectory.mkdirs();
		}
		return newDirectory;
	}

	public static boolean itsDOI(String text) {
		final String pattern = "\\b(10[.][0-9]{4,}(?:[.][0-9]+)*/(?:(?![\"&\\'<>])\\S)+)\\b";
		final Pattern regexPattern = Pattern.compile(pattern);
		final Matcher matcher = regexPattern.matcher(text);
		return matcher.find();
	}

	public static String getDOI(String text) {
		final String pattern = "\\b(10[.][0-9]{4,}(?:[.][0-9]+)*/(?:(?![\"&\\'<>])\\S)+)\\b";
		final Pattern regexPattern = Pattern.compile(pattern);
		final Matcher matcher = regexPattern.matcher(text);
		if (matcher.find()) {
			return matcher.group(0);
		} else {
			return null;
		}
	}

	public static Map<String, String> readMetadata(String directory) {
		BufferedReader bufferedReader = null;
		String line = "";
		final Map<String, String> doiMap = new HashMap<>();
		try {
			final File file = new File(directory + File.separator + METADATA_FILE);
			if (!file.exists()) {
				file.createNewFile();
			}
			bufferedReader = new BufferedReader(new FileReader(file));
			while ((line = bufferedReader.readLine()) != null) {
				final String[] tokens = line.split(SEMICOLON_DELIMITER);
				if (tokens.length > 0) {
					doiMap.put(tokens[0], tokens[1]);
				}
			}
		} catch (final IOException e) {
		} finally {
			if (bufferedReader != null) {
				try {
					bufferedReader.close();
				} catch (final IOException e) {
				}
			}
		}
		return doiMap;

	}

	public static Map<String, List<String>> readDoiInMetaData(String directory, String doi) {
		BufferedReader bufferedReader = null;
		String line = "";
		final List<String> doiList = new LinkedList<>();
		final Map<String, List<String>> doiMap = new HashMap<>();
		try {
			final File file = new File(directory + File.separator + METADATA_FILE);
			if (!file.exists()) {
				file.createNewFile();
			}
			bufferedReader = new BufferedReader(new FileReader(file));
			while ((line = bufferedReader.readLine()) != null) {
				final String[] tokens = line.split(SEMICOLON_DELIMITER);
				if (tokens.length > 0 && tokens[0].equals(doi)) {
					doiList.add(tokens[1]);
				}
			}
			doiMap.put(doi, doiList);
		} catch (final IOException e) {
		} finally {
			if (bufferedReader != null) {
				try {
					bufferedReader.close();
				} catch (final IOException e) {
				}
			}
		}
		return doiMap;

	}

	public static void writeMetadata(String directory, String doi, String title, String date, List<String> authorList,
			boolean isCompletePaper) {
		try (FileWriter metadataFile = new FileWriter(directory + File.separator + METADATA_FILE, true)) {
			final String type = isCompletePaper ? "full" : "abstract";
			String authors = "";
			for (int i = 0; i < authorList.size(); i++) {
				if (i != (authorList.size() - 1)) {
					authors = authors + authorList.get(i) + " - ";
				} else {
					authors = authors + authorList.get(i);
				}
			}
			metadataFile.write(doi + SEMICOLON_DELIMITER + type + SEMICOLON_DELIMITER + title + SEMICOLON_DELIMITER
					+ date + SEMICOLON_DELIMITER + authors + "\n");
		} catch (final IOException e) {
		}
	}

}
