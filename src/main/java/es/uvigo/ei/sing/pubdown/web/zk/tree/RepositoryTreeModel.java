package es.uvigo.ei.sing.pubdown.web.zk.tree;

import static java.util.Arrays.asList;
import static java.util.Collections.sort;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.TreeMap;

import org.zkoss.zul.AbstractTreeModel;

import es.uvigo.ei.sing.pubdown.web.entities.RepositoryQuery;
import es.uvigo.ei.sing.pubdown.web.entities.User;

/**
 * Represents a tree for the {@link User} {@link RepositoryQuery}
 * 
 */
public class RepositoryTreeModel extends AbstractTreeModel<CustomTreeNode> {
	private static final long serialVersionUID = 1L;

	/**
	 * Constructs a {@link RepositoryTreeModel}
	 * 
	 * @param repositoryQueryByCategories
	 *            all the {@link RepositoryQuery} of an {@link User} sorted by
	 *            categories
	 */
	public RepositoryTreeModel(final Map<String, List<RepositoryQuery>> repositoryQueryByCategories) {
		super(createRootNode(repositoryQueryByCategories));
	}

	/**
	 * Creates a {@link RepositoryTreeModel} with {@link RepositoryTreeNode} and
	 * {@link RepositoryQueryTreeNode}.
	 * 
	 * @param repositoryQuerysByRepository
	 *            all the {@link RepositoryQuery} of an {@link User} sorted by
	 *            categories
	 * @return a sorted {@link RepositoryTreeModel}
	 */
	private static CustomTreeNode createRootNode(Map<String, List<RepositoryQuery>> repositoryQuerysByRepository) {
		final RepositoryTreeNode root = new RepositoryTreeNode();
		repositoryQuerysByRepository = new TreeMap<>(repositoryQuerysByRepository);

		repositoryQuerysByRepository.forEach((key, value) -> {
			final String repository = key;
			final List<RepositoryQuery> repositoryQuerys = value;

			final RepositoryTreeNode categoryNode = repository.isEmpty() ? root
					: new RepositoryTreeNode(repository, root);

			sort(repositoryQuerys, (r1, r2) -> r1.getName().compareTo(r2.getName()));
			for (final RepositoryQuery reopositoryQuery : repositoryQuerys) {
				new RepositoryQueryTreeNode(reopositoryQuery, categoryNode);
			}
		});

		return root;
	}

	/**
	 * Returns the root node of the {@link RepositoryTreeModel}
	 * 
	 * @return the root node
	 */
	@Override
	public RepositoryTreeNode getRoot() {
		return (RepositoryTreeNode) super.getRoot();
	}

	/**
	 * Checks if the {@link CustomTreeNode} has children
	 * 
	 * @param node
	 *            the node to check if has children
	 * @return <code>true</code> if the {@link CustomTreeNode} has children,
	 *         <code>false</code> otherwise
	 */
	@Override
	public boolean isLeaf(final CustomTreeNode node) {
		return node.getChildren().isEmpty();
	}

	/**
	 * Returns a child of a {@link CustomTreeNode}
	 * 
	 * @param parent
	 *            the {@link CustomTreeNode}
	 * @param index
	 *            the {@link CustomTreeNode} child index
	 * 
	 * @return a {@link CustomTreeNode} child
	 */
	@Override
	public CustomTreeNode getChild(final CustomTreeNode parent, final int index) {
		return parent.getChildren().get(index);
	}

	/**
	 * Returns the {@link CustomTreeNode} number of children
	 * 
	 * @param parent
	 *            the {@link CustomTreeNode}
	 * @return the number of children
	 */
	@Override
	public int getChildCount(final CustomTreeNode parent) {
		return parent.getChildren().size();
	}

	/**
	 * Returns a selected {@link RepositoryQuery}
	 * 
	 * @return a {@link RepositoryQuery} if the {@link CustomTreeNode} is a
	 *         {@link RepositoryQueryTreeNode} instance, <code>null</code>
	 *         otherwise
	 */
	public RepositoryQuery getSelectedRepositoryQuery() {
		final Set<CustomTreeNode> selection = this.getSelection();

		if (selection.size() == 1) {
			final CustomTreeNode selectedNode = selection.iterator().next();

			if (selectedNode instanceof RepositoryQueryTreeNode) {
				return ((RepositoryQueryTreeNode) selectedNode).getRepositoryQuery();
			}
		}

		return null;
	}

	/**
	 * Sets a {@link RepositoryQuery} as the current selection
	 * 
	 * @param repositoryQuery
	 *            the {@link RepositoryQuery}
	 */
	public void setSelectedRepositoryQuery(final RepositoryQuery repositoryQuery) {
		this.clearSelection();

		final RepositoryQueryTreeNode node = getRepositoryQueryNode(repositoryQuery);

		if (node != null)
			this.setSelection(asList(node));
	}

	/**
	 * Returns a {@link RepositoryTreeNode}
	 * 
	 * @param category
	 *            the {@link RepositoryTreeNode} label to find
	 * @return a {@link RepositoryTreeNode}
	 */
	public RepositoryTreeNode getRepositoryTreeNode(final String category) {
		final CustomTreeNode root = this.getRoot();

		for (int i = 0; i < root.getChildren().size(); i++) {
			final CustomTreeNode repositoryTreeNode = root.getChildren().get(i);
			if (repositoryTreeNode instanceof RepositoryTreeNode) {
				final RepositoryTreeNode categoryNode = (RepositoryTreeNode) repositoryTreeNode;
				if (categoryNode.getLabel().equals(category)) {
					return (RepositoryTreeNode) repositoryTreeNode;
				}
			}
		}

		return null;
	}

	/**
	 * Returns a {@link RepositoryQueryTreeNode}
	 * 
	 * @param repositoryQuery
	 *            the {@link RepositoryQuery} label to find
	 * @return a {@link RepositoryQueryTreeNode}
	 */
	public RepositoryQueryTreeNode getRepositoryQueryNode(final RepositoryQuery repositoryQuery) {
		if (repositoryQuery == null)
			return null;

		final Queue<CustomTreeNode> nodes = new LinkedList<>(this.getRoot().getChildren());

		while (!nodes.isEmpty()) {
			final CustomTreeNode node = nodes.poll();

			if (node instanceof RepositoryTreeNode) {
				final RepositoryTreeNode categoryNode = (RepositoryTreeNode) node;

				nodes.addAll(categoryNode.getChildren());
			} else if (node instanceof RepositoryQueryTreeNode) {
				final RepositoryQueryTreeNode repositoryQueryNode = (RepositoryQueryTreeNode) node;

				if (repositoryQueryNode.getRepositoryQuery().equals(repositoryQuery)) {
					return repositoryQueryNode;
				}
			}
		}

		return null;
	}

	/**
	 * Moves a {@link RepositoryQuery} from a category to another
	 * 
	 * @param repositoryQuery
	 *            the {@link RepositoryQuery}
	 * @param repository
	 *            the category
	 * 
	 * @throws IllegalArgumentException
	 *             if the category is null or if the {@link RepositoryQuery}
	 *             doesn't belong to the tree
	 * 
	 */
	public void moveRepositoryQueryToRepository(final RepositoryQuery repositoryQuery, String repository) {
		if (repository == null)
			throw new IllegalArgumentException("category can't be null");

		final RepositoryQueryTreeNode repositoryQueryNode = this.getRepositoryQueryNode(repositoryQuery);
		if (repositoryQueryNode == null)
			throw new IllegalArgumentException("repositoryQuery doesn't belongs to this tree");

		repository = repository.trim();
		final RepositoryTreeNode previousCategoryNode = repositoryQueryNode.getParent();
		final RepositoryTreeNode categoryNode = getOrCreateRepositoryNode(repository);

		repositoryQuery.setRepository(repository);
		repositoryQueryNode.setParent(categoryNode);

		if (previousCategoryNode.getChildren().isEmpty()) {
			previousCategoryNode.setParent(null);
		}
	}

	/**
	 * Removes a {@link RepositoryQuery} in the {@link RepositoryTreeModel}
	 * 
	 * @param repositoryQuery
	 *            the {@link RepositoryQuery} to remove
	 */
	public void removeRepositoryQuery(final RepositoryQuery repositoryQuery) {
		final RepositoryQueryTreeNode repositoryQueryNode = this.getRepositoryQueryNode(repositoryQuery);
		final RepositoryTreeNode previousCategoryNode = repositoryQueryNode.getParent();

		previousCategoryNode.effectiveRemoveChild(repositoryQueryNode);

		if (previousCategoryNode.getChildren().isEmpty()) {
			previousCategoryNode.setParent(null);
		}
	}

	/**
	 * Adds a {@link RepositoryQuery} in the {@link RepositoryTreeModel}
	 * 
	 * @param repositoryQuery
	 *            the {@link RepositoryQuery} to add
	 * 
	 * @throws IllegalArgumentException
	 *             if the {@link RepositoryQuery} doesn't belong to the tree
	 */
	public void addRepositoryQuery(final RepositoryQuery repositoryQuery) {
		if (this.getRepositoryQueryNode(repositoryQuery) != null)
			throw new IllegalArgumentException("RepositoryQuery already belongs to this tree");

		final RepositoryTreeNode categoryNode = getOrCreateRepositoryNode(repositoryQuery.getRepository());

		new RepositoryQueryTreeNode(repositoryQuery, categoryNode);
	}

	public void addRepository(final String repositoryName) {
		new RepositoryTreeNode(repositoryName, this.getRoot());
	}

	/**
	 * Gets or creates a {@link RepositoryTreeNode}
	 * 
	 * @param repository
	 *            the {@link RepositoryTreeNode} label
	 * @return a {@link RepositoryTreeNode}
	 */
	private RepositoryTreeNode getOrCreateRepositoryNode(final String repository) {
		RepositoryTreeNode categoryTreeNode = this.getRepositoryTreeNode(repository);
		if (categoryTreeNode == null) {
			categoryTreeNode = repository.isEmpty() ? this.getRoot()
					: new RepositoryTreeNode(repository, this.getRoot());
		}

		return categoryTreeNode;
	}

}
