package es.uvigo.ei.sing.pubdown.paperdown.downloader;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.io.RandomAccessFile;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.zkoss.zul.Filedownload;

import es.uvigo.ei.sing.pubdown.web.entities.GlobalConfiguration;
import es.uvigo.ei.sing.pubdown.web.entities.RepositoryQuery;
import es.uvigo.ei.sing.pubdown.web.zk.util.DesktopTransactionManager;
import es.uvigo.ei.sing.pubdown.web.zk.util.TransactionManager;

public class RepositoryManager {
	private static final TransactionManager tm = new DesktopTransactionManager();

	private static final String PDF_FILE_EXTENSION = ".pdf";
	private static final String TXT_FILE_EXTENSION = ".txt";
	private static final String LOG_FILE = "log.csv";
	private static final String SEMICOLON_DELIMITER = ";";
	private static final String METADATA_FILE = "metadata.csv";
	private static final String ZIP_CONTENT_TYPE = "application/zip";

	private static String repositoryPath;

	public static String getRepositoryPath() {
		tm.runInTransaction(em -> {
			em.clear();
			repositoryPath = em
					.createQuery("SELECT g FROM GlobalConfiguration g WHERE	g.configurationKey = :path",
							GlobalConfiguration.class)
					.setParameter("path", "repositoryPath").getSingleResult().getConfigurationValue();
		});
		return repositoryPath;
	}

	public static void updateRepositoryQuery(final RepositoryQuery repositoryQuery) {
		tm.runInTransaction(em -> {
			em.merge(repositoryQuery);
		});
	}

	public static void generatePDFFile(final String pdfURL, final String fileName, final String directory,
			final String directorySuffix, final boolean isCompletePaper, final boolean convertPDFtoTXT,
			final boolean keepPDF, final boolean directoryType) {
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

	public static void generateTXTFile(final String fileName, final String abstractText, final String directory,
			final String directorySuffix, final boolean isCompletePaper, final boolean directoryType) {
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

	private static void convertPDFFiletoTXTFile(final File file, final String directory, final String directorySuffix,
			final boolean isCompletePaper, final boolean keepPDF, final boolean directoryType) {
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

	private static boolean checkIfCorruptedPDF(final File file) {
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

	private static File createDirectory(final String directory) {
		final File newDirectory = new File(directory);
		if (!newDirectory.exists()) {
			newDirectory.mkdirs();
		}
		return newDirectory;
	}

	public static boolean itsDOI(final String text) {
		final String pattern = "\\b(10[.][0-9]{4,}(?:[.][0-9]+)*/(?:(?![\"&\\'<>])\\S)+)\\b";
		final Pattern regexPattern = Pattern.compile(pattern);
		final Matcher matcher = regexPattern.matcher(text);
		return matcher.find();
	}

	public static String getDOI(final String text) {
		final String pattern = "\\b(10[.][0-9]{4,}(?:[.][0-9]+)*/(?:(?![\"&\\'<>])\\S)+)\\b";
		final Pattern regexPattern = Pattern.compile(pattern);
		final Matcher matcher = regexPattern.matcher(text);
		if (matcher.find()) {
			return matcher.group(0);
		} else {
			return null;
		}
	}

	public static Map<String, String> readMetaData(final String directory) {
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

	public static Map<String, List<String>> readDOIInMetaData(final String directory, final String doi) {
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

	public static void writeMetaData(final String directory, final String doi, final String title, final String date,
			final List<String> authorList, final boolean isCompletePaper) {
		try (FileWriter metaDataFile = new FileWriter(directory + File.separator + METADATA_FILE, true)) {
			final String type = isCompletePaper ? "full" : "abstract";
			String authors = "";
			for (int i = 0; i < authorList.size(); i++) {
				if (i != (authorList.size() - 1)) {
					authors = authors + authorList.get(i) + " - ";
				} else {
					authors = authors + authorList.get(i);
				}
			}
			metaDataFile.write(doi + SEMICOLON_DELIMITER + type + SEMICOLON_DELIMITER + title + SEMICOLON_DELIMITER
					+ date + SEMICOLON_DELIMITER + authors + "\n");
		} catch (final IOException e) {
		}
	}

	public static void zipDirectory(String zipFileName, String directoryPath) {
		try {
			File dirObj = new File(directoryPath);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ZipOutputStream zos = new ZipOutputStream(baos);
			addDir(dirObj, zos);
			zos.close();
			Filedownload.save(baos.toByteArray(), ZIP_CONTENT_TYPE, zipFileName);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	static void addDir(File dirObj, ZipOutputStream zos) {
		File[] files = dirObj.listFiles();
		byte[] tmpBuf = new byte[1024];

		for (int i = 0; i < files.length; i++) {
			try {
				if (files[i].isDirectory()) {
					addDir(files[i], zos);
					continue;
				}
				FileInputStream in = new FileInputStream(files[i].getAbsolutePath());

				final String relativePath = dirObj.toURI().relativize(files[i].toURI()).getPath();

				if (!relativePath.equals(LOG_FILE) && !relativePath.equals(METADATA_FILE)) {
					zos.putNextEntry(new ZipEntry(relativePath));
					int len;
					while ((len = in.read(tmpBuf)) > 0) {
						zos.write(tmpBuf, 0, len);
					}
					zos.closeEntry();
				}
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static List<String> readPaperTitleFromLog(final String csvPath) {
		final List<String> titles = new LinkedList<>();
		Stream<String> stream = null;
		try {
			stream = Files.lines(Paths.get(csvPath + LOG_FILE));
			if (stream != null) {
				stream.forEach((line) -> {
					final String[] doi = line.split(SEMICOLON_DELIMITER);
					final String paperTitle = doi[1];
					titles.add(paperTitle);
				});
			}
		} catch (final IOException e) {
		}

		return titles;
	}

}
