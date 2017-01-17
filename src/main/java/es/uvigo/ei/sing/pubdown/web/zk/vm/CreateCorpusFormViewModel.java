package es.uvigo.ei.sing.pubdown.web.zk.vm;

import static es.uvigo.ei.sing.pubdown.util.Checks.isEmpty;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.zkoss.bind.ValidationContext;
import org.zkoss.bind.Validator;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.bind.validator.AbstractValidator;
import org.zkoss.zul.Filedownload;

import es.uvigo.ei.sing.pubdown.paperdown.downloader.RepositoryManager;
import es.uvigo.ei.sing.pubdown.web.entities.Repository;
import es.uvigo.ei.sing.pubdown.web.entities.User;
import weka.core.Instances;
import weka.core.Stopwords;
import weka.core.converters.ConverterUtils.DataSink;
import weka.core.converters.TextDirectoryLoader;
import weka.core.stopwords.StopwordsHandler;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.StringToWordVector;

public class CreateCorpusFormViewModel {
	private static final String TEMPORAL_DIRECTORY = System.getProperty("java.io.tmpdir") + File.separator;

	private Map<Repository, String> repositoriesMap;
	private String arffName;
	private User user;

	@Init
	public void init(@ExecutionArgParam("repositories") final List<Repository> repositories) {
		this.repositoriesMap = new HashMap<>();
		for (Repository repository : repositories) {
			this.repositoriesMap.put(repository, "");
		}
		this.user = repositories.get(0).getUser();
		sortRepositoriesMap();
	}

	private void sortRepositoriesMap() {
		this.repositoriesMap = this.repositoriesMap.entrySet().stream()
				.sorted((e1, e2) -> e1.getKey().getName().compareTo(e2.getKey().getName()))
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2, LinkedHashMap::new));
	}

	public Map<Repository, String> getRepositoriesMap() {
		return repositoriesMap;
	}

	public String getArffName() {
		return arffName;
	}

	public void setArffName(String arffFileName) {
		this.arffName = arffFileName;
	}

	public boolean isRepositoryClassEmpty() {
		for (Map.Entry<Repository, String> entry : this.repositoriesMap.entrySet()) {
			String repositoryClassName = entry.getValue().trim();
			if (!repositoryClassName.isEmpty()) {
				return true;
			}
		}
		return false;
	}

	public boolean isValidCorpus() {
		return !isEmpty(this.arffName) && isRepositoryClassEmpty();
	}

	@Command
	@NotifyChange({ "validCorpus", "repositoryClassEmpty" })
	public void checkCorpus() {
	}

	public Validator getArffNameValidator() {
		return new AbstractValidator() {
			@Override
			public void validate(final ValidationContext ctx) {
				final String name = (String) ctx.getProperty().getValue();

				if (isEmpty(name)) {
					addInvalidMessage(ctx, "Name can't be empty");
				}
			}
		};
	}

	@Command
	public void confirm() {
		final String userLogin = this.user.getLogin();
		final String basePath = RepositoryManager.getRepositoryPath() + File.separator;

		this.repositoriesMap.forEach((repository, repositoryClassName) -> {
			repositoryClassName = repositoryClassName.trim();

			if (!repositoryClassName.isEmpty()) {
				final String repositoryPath = basePath + userLogin + File.separator + repository.getPath();

				final File directory = new File(repositoryPath);

				final String tmpDir = TEMPORAL_DIRECTORY + userLogin + File.separator + this.arffName + File.separator
						+ repositoryClassName;

				RepositoryManager.checkIfDirectoryExist(tmpDir);

				RepositoryManager.copyFilesInRepository(directory, tmpDir);
			}
		});

		final String tmpDir = TEMPORAL_DIRECTORY + userLogin + File.separator + this.arffName;
		TextDirectoryLoader textDirectoryLoader = new TextDirectoryLoader();
		try {
			textDirectoryLoader.setDirectory(new File(tmpDir));
			textDirectoryLoader.setCharSet("UTF-8");

			Instances rawData = textDirectoryLoader.getDataSet();

			StringToWordVector stringToWordVector = new StringToWordVector();
			stringToWordVector.setLowerCaseTokens(true);
			stringToWordVector.setWordsToKeep(10000);
			stringToWordVector.setIDFTransform(true);
			stringToWordVector.setStopwordsHandler(
					new CustomStopWordsHandler("([\\W+]|[0-9]|^[a-zA-Z]$|@|_|n\\/a|[\\%\\€\\$\\£])"));
			try {
				stringToWordVector.setInputFormat(rawData);

				Instances dataFiltered = Filter.useFilter(rawData, stringToWordVector);

				final String arffFile = tmpDir + File.separator + this.arffName + ".arff";
				final DataSink dataSink = new DataSink(arffFile);
				dataSink.write(dataFiltered);
				Filedownload.save(new File(arffFile), null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private class CustomStopWordsHandler implements StopwordsHandler {
		private final Pattern pattern;
		private Stopwords stopWords = new Stopwords();

		public CustomStopWordsHandler(String regexString) {
			pattern = Pattern.compile(regexString);
		}

		@Override
		public boolean isStopword(String s) {
			if(stopWords.is(s)){
				return true;
			}
			Matcher matcher = pattern.matcher(s);
			return matcher.find();
		}
	}

}
