package es.uvigo.ei.sing.pubdown.web.zk.vm;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.annotation.Init;

import es.uvigo.ei.sing.pubdown.paperdown.downloader.RepositoryManager;
import es.uvigo.ei.sing.pubdown.web.entities.Repository;

public class CreateCorpusFormViewModel {
	private static final String TEMPORAL_DIRECTORY = "/tmp/";

	private Map<Repository, String> repositoriesMap;
	private String arffName;

	@Init
	public void init(@ExecutionArgParam("repositories") final List<Repository> repositories) {
		this.repositoriesMap = new HashMap<>();
		for (Repository repository : repositories) {
			this.repositoriesMap.put(repository, "");
		}

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

	@Command
	public void confirm() {
		final String basePath = RepositoryManager.getRepositoryPath() + File.separator;

		System.out.println("--------------------------------------------------");
		this.repositoriesMap.forEach((repository, repositoryClassName) -> {

			repositoryClassName = repositoryClassName.trim();

			if (!repositoryClassName.isEmpty()) {
				final String userLogin = repository.getUser().getLogin();
				final String repositoryPath = basePath + userLogin + File.separator + repository.getPath();

				final File directory = new File(repositoryPath);

				final String temporalDirectory = TEMPORAL_DIRECTORY + userLogin + File.separator + this.arffName
						+ File.separator + repositoryClassName;

				RepositoryManager.checkIfDirectoryExist(temporalDirectory);

				RepositoryManager.copyFilesInRepository(directory, temporalDirectory);

				// filesInRepository.forEach(fir -> {
				// System.out.println(fir);
				// });

				// TextDirectoryLoader textDirectoryLoader = new
				// TextDirectoryLoader();
				// try {
				// textDirectoryLoader.setDirectory(new
				// File(temporalDirectory));
				// textDirectoryLoader.run(textDirectoryLoader,
				// textDirectoryLoader.getOptions());
				// StringToWordVector stringToWordVector = new
				// StringToWordVector();
				// stringToWordVector.setDoNotOperateOnPerClassBasis(true);
				// stringToWordVector.setLowerCaseTokens(true);
				// stringToWordVector.setWordsToKeep(1000000);
				// stringToWordVector.setIDFTransform(true);
				// stringToWordVector.run(stringToWordVector,
				// stringToWordVector.getOptions());
				// try {
				// Instances data = textDirectoryLoader.getDataSet();
				//
				// System.out.println(data.toString());
				//
				// // BufferedWriter writer = new BufferedWriter(new
				// // FileWriter("/tmp/test.arff"));
				// // System.out.println("START WRITING");
				// // writer.write(data.toString());
				// // writer.flush();
				// // writer.close();
				// // System.out.println("FINISH WRITING");
				// } catch (Exception e) {
				// e.printStackTrace();
				// }
				// } catch (IOException e) {
				// e.printStackTrace();
				// }

				// Instances structure;
				// try {
				// structure = textDirectoryLoader.getStructure();
				// System.out.println(structure);
				// textDirectoryLoader.getNextInstance(structure);
				// Instance temp;
				// do {
				// temp = textDirectoryLoader.getNextInstance(structure);
				// if (temp != null) {
				// System.out.println(temp);
				// }
				// } while (temp != null);
				// } catch (IOException e) {
				// e.printStackTrace();
				// }

			}

		});
		System.out.println("--------------------------------------------------");

	}

	@Command
	public void refresh() {
		System.out.println("REFRESH");
	}
}
