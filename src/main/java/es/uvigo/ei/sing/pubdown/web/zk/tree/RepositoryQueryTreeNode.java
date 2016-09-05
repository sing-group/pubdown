package es.uvigo.ei.sing.pubdown.web.zk.tree;

import static java.util.Collections.emptyList;

import java.util.List;

import es.uvigo.ei.sing.pubdown.web.entities.RepositoryQuery;

/**
 * Represents a {@link RepositoryQuery} node in the {@link RepositoryTreeModel}
 * 
 *
 */
public class RepositoryQueryTreeNode extends CategorizableRepositoryTreeNode {
	private final RepositoryQuery repositoryQuery;

	/**
	 * Constructs a {@link RepositoryQueryTreeNode}
	 * 
	 * @param repositoryQuery
	 *            the {@link RepositoryQuery} of the {@link RepositoryQueryTreeNode}
	 * @param parent
	 *            the parent of the {@link RepositoryQueryTreeNode}
	 */
	public RepositoryQueryTreeNode(final RepositoryQuery repositoryQuery, final RepositoryTreeNode parent) {
		super(parent);
		this.repositoryQuery = repositoryQuery;
	}

	/**
	 * Returns the {@link RepositoryQuery} of the {@link RepositoryQueryTreeNode}
	 * 
	 * @return the {@link RepositoryQuery}
	 */
	public RepositoryQuery getRepositoryQuery() {
		return this.repositoryQuery;
	}

	/**
	 * Gets the {@link RepositoryQueryTreeNode} label
	 * 
	 * @return the {@link RepositoryQuery} name
	 */
	@Override
	public String getLabel() {
		return this.repositoryQuery.getName();
	}

	/**
	 * Checks if is a category node
	 *
	 * @return <code>false</code>
	 */
	@Override
	public boolean isRepository() {
		return false;
	}

	/**
	 * Returns an empty list because the {@link RepositoryQueryTreeNode} can't have
	 * children
	 */
	@Override
	public List<CustomTreeNode> getChildren() {
		return emptyList();
	}

	/**
	 * 
	 * @param child
	 *            the child to append
	 * @throws UnsupportedOperationException
	 *             if there is a try to append a child in a
	 *             {@link RepositoryQueryTreeNode}
	 */
	@Override
	public void appendChild(final CustomTreeNode child) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @param child
	 *            the child to remove
	 * 
	 * @throws UnsupportedOperationException
	 *             if there is a try to remove a child in a
	 *             {@link RepositoryQueryTreeNode}
	 */
	@Override
	public void removeChild(final CustomTreeNode child) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Returns the label of a {@link RepositoryQueryTreeNode}
	 * 
	 * @return the {@link RepositoryQuery} name
	 */
	@Override
	public String toString() {
		return getLabel();
	}
}